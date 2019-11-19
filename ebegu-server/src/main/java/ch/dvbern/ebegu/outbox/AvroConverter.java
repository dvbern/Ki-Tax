/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.outbox;

import java.io.ByteArrayOutputStream;

import javax.annotation.Nonnull;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;

public final class AvroConverter {

	private AvroConverter() {
		// util class
	}

	@Nonnull
	public static <D extends SpecificRecordBase> byte[] toAvroBinary(@Nonnull D datum) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
			DatumWriter<D> writer = new SpecificDatumWriter<>(datum.getSchema());

			writer.write(datum, encoder);
			encoder.flush();
			out.close();

			return out.toByteArray();
		} catch (Exception e) {
			throw new IllegalStateException("failed converting to avro", e);
		}
	}

	@Nonnull
	public static <D extends SpecificRecordBase> D fromAvroBinary(
		@Nonnull Schema writer,
		@Nonnull Schema reader,
		@Nonnull byte[] avro) {
		try {
			DatumReader<D> datumReader = new SpecificDatumReader<>(writer, reader);
			Decoder decoder = DecoderFactory.get().binaryDecoder(avro, null);

			return datumReader.read(null, decoder);
		} catch (Exception e) {
			throw new IllegalStateException("failed converting from avro", e);
		}
	}

	/**
	 * @deprecated DANGEROUS: cannot handle Schema changes, because it assumes writer schema matches schema of DTOs in
	 * classpath. Only serves demonstration purposes.
	 */
	@Deprecated
	@Nonnull
	public static <D extends SpecificRecordBase> D fromAvroBinary(@Nonnull Schema writer, @Nonnull byte[] avro) {
		try {
			DatumReader<D> reader = new SpecificDatumReader<>(writer);
			Decoder decoder = DecoderFactory.get().binaryDecoder(avro, null);

			return reader.read(null, decoder);
		} catch (Exception e) {
			throw new IllegalStateException("failed converting from avro", e);
		}
	}

	@Nonnull
	public static <D extends GenericRecord> D fromAvroBinaryGeneric(@Nonnull Schema schema, @Nonnull byte[] avro) {
		try {

			GenericDatumReader<D> reader = new GenericDatumReader<>(schema);
			Decoder decoder = DecoderFactory.get().binaryDecoder(avro, null);

			return reader.read(null, decoder);
		} catch (Exception e) {
			throw new IllegalStateException("failed converting from avro", e);
		}
	}
}
