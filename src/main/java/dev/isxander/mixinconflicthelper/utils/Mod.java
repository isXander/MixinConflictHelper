package dev.isxander.mixinconflicthelper.utils;

import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModDependency;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record Mod(String id, String name, List<String> conflicts, Optional<String> issuesUrl, Optional<String> sourcesUrl) {
    public static Mod fromModContainer(ModContainer mod) {
        return new Mod(
                mod.getMetadata().getId(),
                mod.getMetadata().getName(),
                mod.getMetadata().getDependencies().stream()
                        .filter((dep) -> dep.getKind() == ModDependency.Kind.CONFLICTS)
                        .map(ModDependency::getModId)
                        .toList(),
                mod.getMetadata().getContact().get("issues")
                        .or(() -> mod.getMetadata().getContact().get("sources")),
                mod.getMetadata().getContact().get("sources")
        );
    }

    public static Mod fromDataInputStream(DataInputStream is) throws IOException {
        var id = is.readUTF();
        var name = is.readUTF();

        var conflictSize = is.readInt();
        var conflicts = new ArrayList<String>(conflictSize);
        for (int i = 0; i < conflictSize; i++) {
            conflicts.add(is.readUTF());
        }

        var issuesUrl = is.readUTF();
        if (issuesUrl.equals("null")) {
            issuesUrl = null;
        }

        var sourcesUrl = is.readUTF();
        if (sourcesUrl.equals("null")) {
            sourcesUrl = null;
        }

        return new Mod(id, name, conflicts, Optional.ofNullable(issuesUrl), Optional.ofNullable(sourcesUrl));
    }

    public void writeTo(DataOutputStream os) {
        try {
            os.writeUTF(id());
            os.writeUTF(name());
            os.writeInt(conflicts().size());
            for (var conflict : conflicts()) {
                os.writeUTF(conflict);
            }
            os.writeUTF(issuesUrl().orElse("null"));
            os.writeUTF(sourcesUrl().orElse("null"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
