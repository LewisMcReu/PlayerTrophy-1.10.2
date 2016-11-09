package com.lewismcreu.playertrophy.common.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import com.lewismcreu.playertrophy.util.CollectionUtil;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * @author Lewis_McReu
 */
public class WorldData extends WorldSavedData
{
	public Collection<Clan> getClans()
	{
		return clans;
	}
	public static WorldData loadFromWorld(World world)
	{
		WorldData data = (WorldData) world.getMapStorage().getOrLoadData(WorldData.class, "playertrophy");
		if (data == null)
		{
			data = new WorldData();
			world.getMapStorage().setData("playertrophy", data);
		}
		return data;
	}

	private int clanIdCounter;
	private Collection<Clan> clans;
	private Set<ChunkPos> claimedChunks;
	// private Set<Bounty> bounties; TODO

	public WorldData()
	{
		super("playertrophy");
		clanIdCounter = 0;
		clans = new ArrayList<>();
		claimedChunks = new TreeSet<>(new Comparator<ChunkPos>()
		{
			@Override
			public int compare(ChunkPos arg0, ChunkPos arg1)
			{
				return arg0.chunkXPos * arg1.chunkZPos;
			}
		});
	}

	public Collection<ChunkPos> getClaimedChunks()
	{
		return Collections.unmodifiableSet(claimedChunks);
	}

	public void claimChunk(Clan clan, ChunkPos pos)
	{
		claimedChunks.add(pos);
		clan.claimChunk(pos);
	}

	public void unclaimChunk(Clan clan, ChunkPos pos)
	{
		claimedChunks.remove(pos);
		clan.unclaimChunk(pos);
	}

	public Clan createClan(UUID creator)
	{
		Clan c = Clan.createClan(creator);
		c.setId(clanIdCounter++);
		clans.add(c);
		return c;
	}

	public Clan findClan(int id)
	{
		return CollectionUtil.find(clans, c -> c.getId(), id);
	}

	public Clan findClan(ChunkPos pos)
	{
		if (!claimedChunks.contains(pos)) for (Clan c : clans)
			if (c.getClaimedChunks().contains(pos)) return c;

		return null;
	}

	public Clan findClan(BlockPos pos)
	{
		return findClan(new ChunkPos(pos));
	}
	
	private static final String clanCounterKey = "clancounter", clanKey = "clans", bountyKey = "bounties";

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		clanIdCounter = nbt.getInteger(clanCounterKey);
		NBTTagList clanList = nbt.getTagList(clanKey, NBT.TAG_COMPOUND);
		for (int i = 0; i < clanList.tagCount(); i++)
			clans.add(new Clan().readFromNBT(clanList.getCompoundTagAt(i)));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		compound.setInteger(clanCounterKey, clanIdCounter);
		NBTTagList clanList = new NBTTagList();
		for (Clan c : clans)
			clanList.appendTag(c.writeToNBT());
		compound.setTag(clanKey, clanList);

		return compound;
	}
}