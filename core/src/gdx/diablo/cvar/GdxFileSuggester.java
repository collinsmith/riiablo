package gdx.diablo.cvar;

import com.google.common.base.Preconditions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GdxFileSuggester implements SuggestionProvider {

  @NonNull
  private final FileHandleResolver RESOLVER;

  @Nullable
  private final FilenameFilter FILTER;

  public GdxFileSuggester(@NonNull FileHandleResolver resolver) {
    this.RESOLVER = Preconditions.checkNotNull(resolver, "resolver cannot be null");
    this.FILTER = null;
  }

  public GdxFileSuggester(@NonNull FileHandleResolver resolver, @NonNull FilenameFilter filter) {
    this.RESOLVER = Preconditions.checkNotNull(resolver, "resolver cannot be null");
    this.FILTER = Preconditions.checkNotNull(filter, "filter cannot be null");
  }

  @Override
  public Collection<String> suggest(@NonNull String str) {
    final String pathSeparator = "/";
    FileHandle handle = RESOLVER.resolve(Gdx.files.getLocalStoragePath());
    String pathPart = null;
    int lastPathSep = str.lastIndexOf(pathSeparator);
    if (lastPathSep != -1) {
      pathPart = str.substring(0, lastPathSep) + pathSeparator;
      FileHandle resolvedHandle = handle.child(pathPart);
      if (resolvedHandle.exists()) {
        handle = resolvedHandle;
      }
    }

    FileHandle[] children = handle.list();
    List<String> matching = new ArrayList<>(children.length);
    for (FileHandle child : children) {
      String fileName = pathPart != null ? pathPart + child.name() : child.name();
      if (!fileName.startsWith(str)) {
        continue;
      }

      if (FILTER == null || FILTER.accept(child.file(), fileName)) {
        if (child.isDirectory()) {
          matching.add(fileName + pathSeparator);
        } else {
          matching.add(fileName);
        }
      }
    }

    Collections.sort(matching, new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        if (o1.endsWith(pathSeparator) && o2.endsWith(pathSeparator)) {
          return o1.compareToIgnoreCase(o2);
        } else if (o1.endsWith(pathSeparator)) {
          return -1;
        } else if (o2.endsWith(pathSeparator)) {
          return 1;
        } else {
          return o1.compareToIgnoreCase(o2);
        }
      }
    });
    return matching;
  }
}
