package net.coreprotect.patch.script;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;

import org.bukkit.entity.EntityType;

import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.Database;
import net.coreprotect.language.Phrase;
import net.coreprotect.language.Selector;
import net.coreprotect.patch.Patch;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.Util;

public class __2_20_0 {

    protected static boolean patch(Statement statement) {
        try {
            if (!Config.getGlobal().TYPE_DATABASE.toLowerCase(Locale.ROOT).equals("sqlite")) {
                try {
                    statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "command MODIFY message VARCHAR(16000), CONVERT TO CHARACTER SET utf8mb4");
                }
                catch (Exception e) {
                    Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "command", Selector.FIRST, Selector.FIRST));
                }

                if (!Patch.continuePatch()) {
                    return false;
                }

                try {
                    String extraInfo = ", CONVERT TO CHARACTER SET utf8mb4";
                    String query = "ALTER TABLE %s MODIFY message VARCHAR(16000)%sextrainfo";
                    Database.sendQueryWithIndex(statement, query, ConfigHandler.prefix + "chat", "", extraInfo);
                }
                catch (Exception e) {
                    Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "chat", Selector.FIRST, Selector.FIRST));
                }

                try {
                    String query = "ALTER TABLE " + ConfigHandler.prefix + "sign ADD COLUMN data TINYINT";
                    Database.sendQueryWithoutIndex(statement, query, "", false);
                }
                catch (Exception e) {
                    Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "sign", Selector.FIRST, Selector.FIRST));
                }
            }
            else {
                try {
                    statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "sign ADD COLUMN data INTEGER;");
                }
                catch (Exception e) {
                    Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "sign", Selector.FIRST, Selector.FIRST));
                }
            }

            if (!Patch.continuePatch()) {
                return false;
            }

            String entityQuery = "SELECT rowid, data FROM " + ConfigHandler.prefix + "block WHERE type = (SELECT id FROM " + ConfigHandler.prefix + "material_map WHERE material='minecraft:spawner' LIMIT 1) ORDER BY rowid ASC";
            String preparedQueryUpdate = "UPDATE " + ConfigHandler.prefix + "block SET data = ? WHERE rowid = ?";
            PreparedStatement preparedStatementUpdate = statement.getConnection().prepareStatement(preparedQueryUpdate);
            Database.beginTransaction(statement);

            ResultSet resultSet = statement.executeQuery(entityQuery);
            while (resultSet.next()) {
                EntityType entityType = EntityType.PIG;
                switch (resultSet.getInt("data")) {
                    case 1:
                        entityType = EntityType.ZOMBIE;
                        break;
                    case 2:
                        entityType = EntityType.SKELETON;
                        break;
                    case 3:
                        entityType = EntityType.SPIDER;
                        break;
                    case 4:
                        entityType = EntityType.CAVE_SPIDER;
                        break;
                    case 5:
                        entityType = EntityType.SILVERFISH;
                        break;
                    case 6:
                        entityType = EntityType.BLAZE;
                        break;
                    default:
                        entityType = EntityType.PIG;
                        break;
                }

                preparedStatementUpdate.setInt(1, Util.getSpawnerType(entityType));
                preparedStatementUpdate.setInt(2, resultSet.getInt("rowid"));
                preparedStatementUpdate.executeUpdate();
            }
            resultSet.close();
            preparedStatementUpdate.close();

            Database.commitTransaction(statement);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

}
