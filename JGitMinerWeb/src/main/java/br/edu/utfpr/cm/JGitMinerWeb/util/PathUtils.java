
package br.edu.utfpr.cm.JGitMinerWeb.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class PathUtils {

    public static boolean isSameFullPath(String file, String file2) {
        String fullPath = FilenameUtils.getFullPath(file);
        String fullPath2 = FilenameUtils.getFullPath(file2);
        return StringUtils.equals(fullPath, fullPath2);
    }
}
