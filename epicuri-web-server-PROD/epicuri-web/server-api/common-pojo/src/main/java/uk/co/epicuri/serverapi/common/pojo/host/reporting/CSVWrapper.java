package uk.co.epicuri.serverapi.common.pojo.host.reporting;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish.
 */
public class CSVWrapper extends DownloadableFile {

    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
