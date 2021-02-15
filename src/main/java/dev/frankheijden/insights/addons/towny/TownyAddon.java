package dev.frankheijden.insights.addons.towny;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.TownClaimEvent;
import com.palmergames.bukkit.towny.event.town.TownUnclaimEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.addons.InsightsAddon;
import dev.frankheijden.insights.api.addons.Region;
import dev.frankheijden.insights.api.objects.chunk.ChunkLocation;
import dev.frankheijden.insights.api.objects.chunk.ChunkPart;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class TownyAddon implements InsightsAddon, Listener {

    private String getKey(Town town) {
        return "TA" + town.getUUID();
    }

    public Optional<Region> adapt(Town town, World world) {
        if (town == null) return Optional.empty();
        return Optional.of(new TownRegion(world, town.getTownBlocks(), getKey(town)));
    }

    @Override
    public String getPluginName() {
        return "Towny";
    }

    @Override
    public String getAreaName() {
        return "town";
    }

    @Override
    public String getVersion() {
        return "{version}";
    }

    @Override
    public Optional<Region> getRegion(Location location) {
        return adapt(TownyAPI.getInstance().getTown(location), location.getWorld());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTownClaim(TownClaimEvent event) {
        try {
            deleteTownCache(event.getTownBlock().getTown());
        } catch (NotRegisteredException ignored) {
            //
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTownUnclaim(TownUnclaimEvent event) {
        deleteTownCache(event.getTown());
    }

    private void deleteTownCache(Town town) {
        InsightsPlugin.getInstance().getAddonStorage().remove(getKey(town));
    }

    public class TownRegion implements Region {

        private final World world;
        private final Collection<TownBlock> townBlocks;
        private final String key;

        public TownRegion(World world, Collection<TownBlock> townBlocks, String key) {
            this.world = world;
            this.townBlocks = townBlocks;
            this.key = key;
        }

        @Override
        public String getAddon() {
            return getPluginName();
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public List<ChunkPart> toChunkParts() {
            List<ChunkPart> parts = new ArrayList<>(townBlocks.size());
            for (TownBlock block : townBlocks) {
                parts.add(new ChunkPart(new ChunkLocation(world, block.getX(), block.getZ())));
            }
            return parts;
        }
    }
}
