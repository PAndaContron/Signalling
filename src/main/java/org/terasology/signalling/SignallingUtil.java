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
package org.terasology.signalling;

import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.math.SideBitFlag;
import org.terasology.world.block.Block;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.signalling.blockFamily.RotationBlockFamily;

public final class SignallingUtil {
    private SignallingUtil() {
    }

    public static Side getResultSide(Block block, Side definedSide) {
        Rotation rotation = getRotation(block);
        return rotation.rotate(definedSide);
    }

    private static Rotation getRotation(Block block) {
        Rotation rotation = Rotation.none();
        BlockFamily blockFamily = block.getBlockFamily();
        if (blockFamily instanceof RotationBlockFamily) {
            rotation = ((RotationBlockFamily) blockFamily).getRotation(block);
        }
        return rotation;
    }

    public static byte getResultConnections(Block block, byte definedSides) {
        Rotation rotation = getRotation(block);

        byte result = 0;
        for (Side side : SideBitFlag.getSides(definedSides)) {
            result = SideBitFlag.addSide(result, rotation.rotate(side));
        }

        return result;
    }

    public static byte getSourceConnections(Block block, byte connections) {
        Rotation rotation = getRotation(block);

        rotation = Rotation.findReverse(rotation);

        byte result = 0;
        for (Side side : SideBitFlag.getSides(connections)) {
            result = SideBitFlag.addSide(result, rotation.rotate(side));
        }

        return result;
    }
}
