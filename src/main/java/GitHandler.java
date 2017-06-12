import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitHandler {

    private final Repository _repository;
    private final Git _git;

    public GitHandler(String gitDir) throws IOException {
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        repositoryBuilder.setMustExist(true);
        File file = new File(gitDir);
        _repository = repositoryBuilder.findGitDir(file).build();
        _git = new Git(_repository);
    }

    public List<RevCommit> getPossibleNopolFixes() throws IOException, GitAPIException {
        final ArrayList<RevCommit> output = new ArrayList<>();
        Iterable<RevCommit> commits = _git.log().all().setRevFilter(RevFilter.NO_MERGES).call();
        for(RevCommit commit : commits) {
            if(commit.getParentCount() < 1)
                continue;
            if(isSmallConditionalBugFix(commit))
                output.add(commit);
        }
        return output;
    }

    private boolean isSmallConditionalBugFix(RevCommit commit) throws IOException {
        final RevCommit parent = commit.getParent(0);
        try(ByteArrayOutputStream os =  new ByteArrayOutputStream();
            DiffFormatter df = new DiffFormatter(os)) {
            df.setReader(_repository.newObjectReader(), _repository.getConfig());
            List<DiffEntry> diffEntries = df.scan(parent.getTree(), commit.getTree());
            if(diffEntries.size() != 1
                    || !diffEntries.get(0).getOldPath().endsWith("java")
                    || countAddedLines(diffEntries, df) > 3) {
                return false;
            }
            df.setContext(0);
            df.format(diffEntries.get(0));
            String codeChange = os.toString();
            return codeChange.matches("(?s).*if[ ]*\\(.*");
            //return codeChange.contains("if (");
        }
    }

    private int countAddedLines(List<DiffEntry> diffEntries, DiffFormatter diffFormatter) throws IOException {
        int lines = 0;
        for (DiffEntry diff : diffEntries) {
            for(Edit edit : diffFormatter.toFileHeader(diffEntries.get(0)).toEditList()) {
                lines += edit.getEndB() - edit.getBeginB();
                //deleted += edit.getEndA() - edit.getBeginA();
            }
        }
        return lines;
    }


}
