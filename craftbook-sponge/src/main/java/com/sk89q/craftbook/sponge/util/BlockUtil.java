/*
 * CraftBook Copyright (C) 2010-2019 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2019 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package com.sk89q.craftbook.sponge.util;

import com.google.common.collect.Sets;
import com.sk89q.craftbook.core.util.RegexUtil;
import com.sk89q.craftbook.core.util.TernaryState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.block.GroundLuminanceProperty;
import org.spongepowered.api.data.property.block.IndirectlyPoweredProperty;
import org.spongepowered.api.data.property.block.PoweredProperty;
import org.spongepowered.api.data.property.block.SkyLuminanceProperty;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public final class BlockUtil {

    private static final Set<BlockType> trackBlocks = Sets.newHashSet(
            BlockTypes.RAIL, BlockTypes.GOLDEN_RAIL, BlockTypes.DETECTOR_RAIL,
            BlockTypes.ACTIVATOR_RAIL
    );

    /**
     * Gets the relative direction of 'other' from 'base'.
     *
     * @param base The location of base.
     * @param other The location of other.
     * @return The relative direction
     */
    public static Direction getFacing(Location<World> base, Location<World> other) {
        for (Direction dir : Direction.values()) {
            if (base.getRelative(dir).getPosition().equals(other.getPosition()))
                return dir;
        }

        return null;
    }

    public static Optional<Integer> getDirectBlockPowerLevel(Location<?> ... blocks) {
        int power = -1;

        for(Location<?> block : blocks) {
            Optional<Integer> optional = getDirectBlockPowerLevel(block);
            if(optional.isPresent()) {
                if (power < optional.get()) {
                    power = optional.get();
                }
            }
        }

        if (power == -1) {
            return Optional.empty();
        } else {
            return Optional.of(power);
        }
    }

    public static Optional<Integer> getDirectBlockPowerLevel(Location<?> block) {
        if (block.get(Keys.POWER).isPresent()) {
            return block.get(Keys.POWER);
        } else if (block.get(Keys.POWERED).isPresent()) {
            return block.get(Keys.POWERED).map((powered -> powered ? 15 : 0));
        } else if (block.getBlock().getType() == BlockTypes.POWERED_REPEATER || block.getBlock().getType() == BlockTypes.UNPOWERED_REPEATER) {
            return Optional.of(block.getBlock().getType() == BlockTypes.POWERED_REPEATER ? 15 : 0);
        }
        return Optional.empty();
    }

    public static Optional<Integer> getBlockPowerLevel(Location<?> block) {
        if (getDirectBlockPowerLevel(block).isPresent()) {
            return getDirectBlockPowerLevel(block);
        } else if (block.getProperty(PoweredProperty.class).isPresent()) {
            return Optional.of(block.getProperty(PoweredProperty.class).get().getValue() ? 15 : 0);
        }
        return Optional.empty();
    }

    public static Optional<Integer> getBlockIndirectPowerLevel(Location<?> block) {
        if (getBlockPowerLevel(block).isPresent()) {
            return getBlockPowerLevel(block);
        } else if (block.getProperty(IndirectlyPoweredProperty.class).isPresent()) {
            return Optional.of(block.getProperty(IndirectlyPoweredProperty.class).get().getValue() ? 15 : 0);
        }

        return Optional.empty();
    }

    /**
     * Checks whether this block is capable of providing power to nearby blocks.
     *
     * @param block The block to check
     * @return If it's a power source.
     */
    public static boolean isPowerSource(Location<World> block) {
        // TODO Make this more mod friendly.
        return block.getBlockType() == BlockTypes.REDSTONE_WIRE
                || block.getBlockType() == BlockTypes.POWERED_REPEATER
                || block.getBlockType() == BlockTypes.UNPOWERED_REPEATER
                || block.getBlockType() == BlockTypes.UNLIT_REDSTONE_TORCH
                || block.getBlockType() == BlockTypes.REDSTONE_TORCH
                || block.getBlockType() == BlockTypes.WOODEN_PRESSURE_PLATE
                || block.getBlockType() == BlockTypes.STONE_PRESSURE_PLATE
                || block.getBlockType() == BlockTypes.LIGHT_WEIGHTED_PRESSURE_PLATE
                || block.getBlockType() == BlockTypes.HEAVY_WEIGHTED_PRESSURE_PLATE
                || block.getBlockType() == BlockTypes.POWERED_COMPARATOR
                || block.getBlockType() == BlockTypes.UNPOWERED_COMPARATOR
                || block.getBlockType() == BlockTypes.REDSTONE_BLOCK
                || block.getBlockType() == BlockTypes.LEVER;
    }

    public static TernaryState isPowered(Location<World> block, Direction face) {
        Location<World> pow = block.getRelative(face);

        if (isPowerSource(pow)) {
            if (getBlockPowerLevel(pow).orElse(0) > 0
                    || getBlockIndirectPowerLevel(pow).orElse(0) > 0) return TernaryState.TRUE;
            return TernaryState.FALSE;
        }
        return TernaryState.NONE;
    }

    /**
     * Gets the combined light level of this block.
     *
     * <p>
     *     Note: This includes both sky and block light levels.
     * </p>
     *
     * @param block The block.
     * @return The light level
     */
    public static int getLightLevel(Location<?> block) {
        Optional<GroundLuminanceProperty> groundLuminanceProperty = block.getProperty(GroundLuminanceProperty.class);
        Optional<SkyLuminanceProperty> skyLuminanceProperty = block.getProperty(SkyLuminanceProperty.class);
        if(groundLuminanceProperty.isPresent() && skyLuminanceProperty.isPresent())
            return (groundLuminanceProperty.get().getValue().intValue() + skyLuminanceProperty.get().getValue().intValue()) / 2;
        else if(groundLuminanceProperty.isPresent())
            return groundLuminanceProperty.get().getValue().intValue();
        else if(skyLuminanceProperty.isPresent())
            return skyLuminanceProperty.get().getValue().intValue();
        return 0;
    }

    public static BlockState getBlockStateFromString(String rule) {
        BlockType blockType;

        Map<String, String> traitSpecifics = new HashMap<>();

        if(rule.contains("[") && rule.endsWith("]")) {
            String subRule = rule.substring(rule.indexOf('['), rule.length()-2);
            String[] parts = RegexUtil.COMMA_PATTERN.split(subRule);

            blockType = Sponge.getGame().getRegistry().getType(BlockType.class, rule.substring(0, rule.indexOf('['))).orElse(null);

            for(String part : parts) {
                String[] keyValue = RegexUtil.EQUALS_PATTERN.split(part);
                traitSpecifics.put(keyValue[0].toLowerCase(), keyValue[1]);
            }
        } else {
            blockType = Sponge.getGame().getRegistry().getType(BlockType.class, rule).orElse(null);
        }

        if(blockType == null) {
            return null;
        }

        BlockState state = blockType.getDefaultState();
        for(Map.Entry<String, String> entry : traitSpecifics.entrySet()) {
            state.getTrait(entry.getKey()).ifPresent((trait) -> state.withTrait(trait, entry.getValue()));
        }

        return state;
    }

    private static Direction[] directFaces = null;

    /**
     * Get faces that are directly touching the block.
     *
     * @return Faces that are directly touching the block.
     */
    public static Direction[] getDirectFaces() {
        if(directFaces == null)
            directFaces = new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        return directFaces;
    }

    private static Direction[] directHorizontalFaces = null;

    public static Direction[] getDirectHorizontalFaces() {
        if (directHorizontalFaces == null)
            directHorizontalFaces = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        return directHorizontalFaces;
    }

    public static List<Location<World>> getAdjacentExcept(Location<World> location, Direction ... directions) {
        List<Location<World>> locations = new ArrayList<>();

        for(Direction direction : getDirectFaces()) {
            boolean passes = true;
            for(Direction direction1 : directions) {
                if(direction1 == direction) {
                    passes = false;
                    break;
                }
            }
            if (passes) {
                locations.add(location.getRelative(direction));
            }
        }

        return locations;
    }

    /**
     * Gets whether or not the specified {@link BlockState} passes the {@link SpongeBlockFilter}.
     *
     * @param filter The filter
     * @param state The state to test
     * @return If it passes
     */
    public static boolean doesStatePassFilter(SpongeBlockFilter filter, BlockState state) {
        return filter.getApplicableBlocks().stream()
                .anyMatch(blockState -> blockState.equals(state));
    }

    /**
     * Gets whether or not the specified {@link BlockState} passes the {@link SpongeBlockFilter}s.
     *
     * @param filters The filters
     * @param state The state to test
     * @return If it passes
     */
    public static boolean doesStatePassFilters(Collection<SpongeBlockFilter> filters, BlockState state) {
        for(SpongeBlockFilter filter : filters)
            if(doesStatePassFilter(filter, state))
                return true;
        return false;
    }

    /**
     * Gets the length of a line of blocks, with a maximum length.
     *
     * @param startBlock The starting location
     * @param testState The block that the line is made of
     * @param direction The direction of the line from the starting block
     * @param maximum The maximum length
     * @return The found length
     */
    public static int getLength(Location<World> startBlock, BlockState testState, Direction direction, int maximum) {
        int length = 0;

        while(length < maximum) {
            if(startBlock.getBlock().equals(testState)) {
                length ++;
                startBlock = startBlock.getRelative(direction);
            } else {
                break;
            }
        }

        return length;
    }

    public static int getMinimumLength(Location<World> firstBlock, Location<World> secondBlock, BlockState testState, Direction direction, int maximum) {
        return Math.min(getLength(firstBlock, testState, direction, maximum), getLength(secondBlock, testState, direction, maximum));
    }

    public static Location<World> getNextMatchingSign(Location<World> block, Direction back, int maximumLength, Predicate<Sign> predicate) {
        for (int i = 0; i < maximumLength; i++) {
            block = block.getRelative(back);
            if (SignUtil.isSign(block)) {
                Sign sign = (Sign) block.getTileEntity().get();

                if (predicate.test(sign)) {
                    return block;
                }
            }
        }
        return null;
    }

    /**
     * Checks if a {@link BlockType} is a Minecart track.
     *
     * @param blockType The blocktype to check.
     * @return If it's a Minecart track
     */
    public static boolean isTrack(BlockType blockType) {
        return trackBlocks.contains(blockType);
    }
}
