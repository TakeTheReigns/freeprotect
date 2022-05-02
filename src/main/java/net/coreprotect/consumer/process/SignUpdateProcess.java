package net.coreprotect.consumer.process;

import java.sql.Statement;
import java.util.Locale;

import org.bukkit.block.BlockState;

import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.Database;
import net.coreprotect.database.statement.SignStatement;
import net.coreprotect.utility.Util;

class SignUpdateProcess {

    static void process(Statement statement, Object object, String user, int action, int time) {
        /*
         * We're switching blocks around quickly.
         * This block could already be removed again by the time the server tries to modify it.
         * Ignore any errors.
         */
        if (object instanceof BlockState) {
            BlockState block = (BlockState) object;
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            int wid = Util.getWorldId(block.getWorld().getName());
            int userid = ConfigHandler.playerIdCache.get(user.toLowerCase(Locale.ROOT));
            String query = "";
            if (action == 0) {
                query = "SELECT color, data, line_1, line_2, line_3, line_4 FROM " + ConfigHandler.prefix + "sign WHERE `user`='" + userid + "' AND wid='" + wid + "' AND x='" + x + "' AND z='" + z + "' AND y='" + y + "' AND time < '" + time + "' ORDER BY rowid DESC" + Database.getOffsetLimit(0, 1);
            }
            else {
                query = "SELECT color, data, line_1, line_2, line_3, line_4 FROM " + ConfigHandler.prefix + "sign WHERE `user`='" + userid + "' AND wid='" + wid + "' AND x='" + x + "' AND z='" + z + "' AND y='" + y + "' AND time >= '" + time + "' ORDER BY rowid ASC" + Database.getOffsetLimit(0, 1);
            }
            query = Database.setCorrectQueryFormat(query);
            SignStatement.getData(statement, block, query);
            Util.updateBlock(block);
        }
    }
}
