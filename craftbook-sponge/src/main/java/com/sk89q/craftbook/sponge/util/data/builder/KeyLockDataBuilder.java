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
package com.sk89q.craftbook.sponge.util.data.builder;

import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.immutable.ImmutableKeyLockData;
import com.sk89q.craftbook.sponge.util.data.mutable.KeyLockData;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.Optional;

public class KeyLockDataBuilder extends AbstractDataBuilder<KeyLockData> implements DataManipulatorBuilder<KeyLockData, ImmutableKeyLockData> {

    public KeyLockDataBuilder() {
        super(KeyLockData.class, 1);
    }

    @Override
    public KeyLockData create() {
        return new KeyLockData();
    }

    @Override
    public Optional<KeyLockData> createFrom(DataHolder dataHolder) {
        return create().fill(dataHolder);
    }

    @Override
    protected Optional<KeyLockData> buildContent(DataView container) throws InvalidDataException {
        if (container.contains(CraftBookKeys.KEY_LOCK.getQuery())) {
            return Optional.of(new KeyLockData(container.getSerializable(CraftBookKeys.KEY_LOCK.getQuery(), ItemStackSnapshot.class).get()));
        }

        return Optional.empty();
    }
}
