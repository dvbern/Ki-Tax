/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

ALTER TABLE received_event ADD success BOOLEAN DEFAULT TRUE;
ALTER TABLE received_event ADD COLUMN IF NOT EXISTS error TEXT;
ALTER TABLE received_event ALTER COLUMN success DROP DEFAULT;

CREATE INDEX IX_received_event_event_id ON received_event(event_id);
CREATE INDEX IX_received_event_event_key ON received_event(event_key);
CREATE INDEX IX_received_event_event_type ON received_event(event_type);
CREATE INDEX IX_received_event_event_timestamp ON received_event(event_timestamp);
CREATE INDEX IX_received_event_success ON received_event(success);
