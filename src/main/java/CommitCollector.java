import au.com.bytecode.opencsv.CSVWriter;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

public class CommitCollector {

    final private GitHandler _gitHandler;
    private List<RevCommit> _commits;

    public CommitCollector(String repoDir) throws IOException {
        _gitHandler = new GitHandler(repoDir);
    }

    public CommitCollector collect() {
        try {
            _commits = _gitHandler.getPossibleNopolFixes();
        } catch (IOException|GitAPIException e) {
            throw new RuntimeException("Unable to execute git log!");
        }
        return this;
    }

    public void writeToCSV(String file) throws IOException {
        if(_commits == null)
            throw new IOException("Nothing to write!");

        try (CSVWriter writer = new CSVWriter(new PrintWriter(file), ';')) {
            _commits.forEach(revCommit -> writer.writeNext(new String[]{
                    revCommit.getId().toString(),
                    revCommit.getFullMessage().replaceAll("\n", " NEWLINE "),
                    new Date((long)revCommit.getCommitTime() * 1000).toString(),
                    revCommit.getParent(0).getId().toString()
            }));
            writer.flush();
        }
    }

}
