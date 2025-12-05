package com.oakshards.afk;

import org.bukkit.Location;
import org.bukkit.World;

public class AFKArea {

    private final String name;
    private World world;
    private Location pos1;
    private Location pos2;
    private int minX, maxX, minY, maxY, minZ, maxZ;
    
    private boolean enabled;
    private int intervalSeconds;
    private int shardsPerInterval;
    private String entryTitle;
    private String entrySubtitle;
    private String leaveTitle;
    private String leaveSubtitle;
    private String actionBarFormat;

    public AFKArea(String name, World world, Location pos1, Location pos2) {
        this.name = name;
        this.world = world;
        this.pos1 = pos1;
        this.pos2 = pos2;
        
        recalculateBounds();
        
        this.enabled = true;
        this.intervalSeconds = 60;
        this.shardsPerInterval = 1;
    }

    private void recalculateBounds() {
        this.minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        this.maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        this.minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        this.maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        this.minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        this.maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
    }

    public boolean contains(Location location) {
        if (location == null || location.getWorld() == null) return false;
        if (!location.getWorld().equals(world)) return false;
        
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }

    public String getName() { return name; }
    public World getWorld() { return world; }
    
    public Location getPos1() { return pos1; }
    public void setPos1(Location pos1) { 
        this.pos1 = pos1; 
        this.world = pos1.getWorld();
        recalculateBounds();
    }
    
    public Location getPos2() { return pos2; }
    public void setPos2(Location pos2) { 
        this.pos2 = pos2; 
        recalculateBounds();
    }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public int getIntervalSeconds() { return intervalSeconds; }
    public void setIntervalSeconds(int intervalSeconds) { this.intervalSeconds = intervalSeconds; }
    
    public int getShardsPerInterval() { return shardsPerInterval; }
    public void setShardsPerInterval(int shardsPerInterval) { this.shardsPerInterval = shardsPerInterval; }
    
    public String getEntryTitle() { return entryTitle; }
    public void setEntryTitle(String entryTitle) { this.entryTitle = entryTitle; }
    
    public String getEntrySubtitle() { return entrySubtitle; }
    public void setEntrySubtitle(String entrySubtitle) { this.entrySubtitle = entrySubtitle; }
    
    public String getLeaveTitle() { return leaveTitle; }
    public void setLeaveTitle(String leaveTitle) { this.leaveTitle = leaveTitle; }
    
    public String getLeaveSubtitle() { return leaveSubtitle; }
    public void setLeaveSubtitle(String leaveSubtitle) { this.leaveSubtitle = leaveSubtitle; }
    
    public String getActionBarFormat() { return actionBarFormat; }
    public void setActionBarFormat(String actionBarFormat) { this.actionBarFormat = actionBarFormat; }
}
