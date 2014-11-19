/*
 * Copyright 2014 MovingBlocks
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
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.signalling.components.SignalTimeDelayComponent;
import org.terasology.signalling.nui.DelayConfigurationScreen;
import org.terasology.world.block.BlockComponent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.CLIENT)
public class SignallingConfigurationSystem extends BaseComponentSystem {
    @In
    private NUIManager nuiManager;

    @ReceiveEvent(components = {BlockComponent.class, SignalTimeDelayComponent.class})
    public void openDelayConfiguration(ActivateEvent event, EntityRef entity) {
        nuiManager.toggleScreen("signalling:delayConfigurationScreen");
        DelayConfigurationScreen layer = (DelayConfigurationScreen) nuiManager.getScreen("signalling:delayConfigurationScreen");
        layer.attachToEntity("Delay configuration", entity);
    }
}
