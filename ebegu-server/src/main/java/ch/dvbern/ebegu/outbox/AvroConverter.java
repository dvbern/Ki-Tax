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
import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.file.SeekableByteArrayInput;
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
	public static <D extends SpecificRecordBase> byte[] toAvroData(@Nonnull D datum) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DatumWriter<D> writer = new SpecificDatumWriter<>(datum.getSchema());
			DataFileWriter<D> dataFileWriter = new DataFileWriter<>(writer);

			dataFileWriter.create(datum.getSchema(), out);
			dataFileWriter.append(datum);
			dataFileWriter.flush();
			dataFileWriter.close();

			return out.toByteArray();
		} catch (IOException e) {
			throw new IllegalStateException("failed converting to avro", e);
		}
	}

	@Nonnull
	public static <D extends SpecificRecordBase> List<D> fromAvroData(@Nonnull Schema schema, @Nonnull byte[] avro) {
		try {
			DatumReader<D> reader = new SpecificDatumReader<>(schema);
			SeekableByteArrayInput inputStream = new SeekableByteArrayInput(avro);
			DataFileReader<D> dataFileReader = new DataFileReader<>(inputStream, reader);

			List<D> datum = Lists.newArrayList(dataFileReader.iterator());

			dataFileReader.close();

			return datum;
		} catch (IOException e) {
			throw new IllegalStateException("failed converting from avro", e);
		}
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
		} catch (IOException e) {
			throw new IllegalStateException("failed converting to avro", e);
		}
	}

	@Nonnull
	public static <D extends SpecificRecordBase> D fromAvroBinary(@Nonnull Schema schema, @Nonnull byte[] avro) {
		try {
			DatumReader<D> reader = new SpecificDatumReader<>(schema);
			Decoder decoder = DecoderFactory.get().binaryDecoder(avro, null);

			return reader.read(null, decoder);
		} catch (IOException e) {
			throw new IllegalStateException("failed converting from avro", e);
		}
	}
}
