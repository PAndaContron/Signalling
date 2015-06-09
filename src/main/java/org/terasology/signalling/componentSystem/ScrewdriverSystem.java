/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.signalling.componentSystem;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.marcinsc.blockFamily.RotationBlockFamily;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.signalling.components.RotateableByScrewdriverComponent;
import org.terasology.signalling.components.ScrewdriverComponent;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.family.SideDefinedBlockFamily;

import java.util.EnumMap;

@RegisterSystem(RegisterMode.AUTHORITY)
public class ScrewdriverSystem extends BaseComponentSystem {
    @In
    private WorldProvider worldProvider;
    @In
    private BlockEntityRegistry blockEntityRegistry;

    private EnumMap<Side, Side> sideOrder = new EnumMap<>(Side.class);

    @Override
    public void initialise() {
        sideOrder.put(Side.FRONT, Side.LEFT);
        sideOrder.put(Side.LEFT, Side.BACK);
        sideOrder.put(Side.BACK, Side.RIGHT);
        sideOrder.put(Side.RIGHT, Side.TOP);
        sideOrder.put(Side.TOP, Side.BOTTOM);
        sideOrder.put(Side.BOTTOM, Side.FRONT);
    }

    @ReceiveEvent(components = {ScrewdriverComponent.class})
    public void rotateGate(ActivateEvent event, EntityRef screwdriver) {
        final EntityRef target = event.getTarget();
        if (target.hasComponent(RotateableByScrewdriverComponent.class)) {
            final Vector3i targetLocation = new Vector3i(event.getTargetLocation());
            final Block block = worldProvider.getBlock(targetLocation);
            final BlockFamily blockFamily = block.getBlockFamily();
            if (blockFamily instanceof SideDefinedBlockFamily) {
                final SideDefinedBlockFamily sideDefinedBlockFamily = (SideDefinedBlockFamily) blockFamily;
                // Figure out the next block and side
                Side newSide = block.getDirection();
                Block blockForSide;
                do {
                    newSide = sideOrder.get(newSide);
                    blockForSide = sideDefinedBlockFamily.getBlockForSide(newSide);
                } while (blockForSide == null);

                worldProvider.setBlock(targetLocation, blockForSide);
            } else if (blockFamily instanceof RotationBlockFamily) {
                RotationBlockFamily rotationBlockFamily = (RotationBlockFamily) blockFamily;
                Side clickedSide = Side.inDirection(event.getHitNormal());
                Block rotatedBlock = getBlockForClockwiseRotation(rotationBlockFamily, block, clickedSide);
                if (rotatedBlock != null) {
                    worldProvider.setBlock(targetLocation, rotatedBlock);
                }
            }
        }
    }

    private Block getBlockForClockwiseRotation(RotationBlockFamily rotationBlockFamily, Block currentBlock, Side sideToRotateAround) {
        // This definitely can be done more efficiently, but I'm too lazy to figure it out and it's going to be
        // invoked once in a blue moon anyway, so we can do it the hard way
        Rotation currentRotation = rotationBlockFamily.getRotation(currentBlock);

        // Pick a side we want to rotate
        SideMapping sideMapping = findSideMappingForSide(sideToRotateAround);

        // Find which side the side we want to keep was originally at
        Side originalSide = findOriginalSide(currentRotation, sideToRotateAround);

        // Find which side we want to rotate was originally at
        Side originalRotatedSide = findOriginalSide(currentRotation, sideMapping.originalSide);

        // This is the side we want the leftRelativeToEndUpAt
        Side resultRotatedSide = sideMapping.resultSide;

        Rotation resultRotation = findDesiredRotation(originalSide, sideToRotateAround, originalRotatedSide, resultRotatedSide);

        return rotationBlockFamily.getBlockForRotation(resultRotation);
    }

    private SideMapping findSideMappingForSide(Side side) {
        switch (side) {
            case TOP:
                return new SideMapping(Side.RIGHT, Side.FRONT);
            case BOTTOM:
                return new SideMapping(Side.RIGHT, Side.BACK);
            case RIGHT:
                return new SideMapping(Side.FRONT, Side.TOP);
            case LEFT:
                return new SideMapping(Side.FRONT, Side.BOTTOM);
            case FRONT:
                return new SideMapping(Side.TOP, Side.RIGHT);
            default:
                return new SideMapping(Side.TOP, Side.LEFT);
        }
    }

    private Rotation findDesiredRotation(Side originalSide, Side relativeSide, Side originalLeftSide, Side resultSide) {
        for (Rotation rotation : Rotation.values()) {
            if (rotation.rotate(originalSide) == relativeSide
                    && rotation.rotate(originalLeftSide) == resultSide) {
                return rotation;
            }
        }
        return null;
    }

    private Side findOriginalSide(Rotation rotation, Side resultSide) {
        for (Side side : Side.values()) {
            if (rotation.rotate(side) == resultSide) {
                return side;
            }
        }

        return null;
    }

    private static class SideMapping {
        private final Side originalSide;
        private final Side resultSide;

        private SideMapping(Side resultSide, Side originalSide) {
            this.resultSide = resultSide;
            this.originalSide = originalSide;
        }
    }
}
