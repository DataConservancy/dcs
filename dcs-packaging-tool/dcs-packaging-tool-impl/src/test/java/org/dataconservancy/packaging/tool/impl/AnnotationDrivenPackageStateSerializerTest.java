/*
 *
 *  * Copyright 2015 Johns Hopkins University
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.dataconservancy.packaging.tool.impl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipLong;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.dataconservancy.packaging.tool.model.ApplicationVersion;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.ser.StreamId;
import org.dataconservancy.packaging.tool.ser.AbstractSerializationTest;
import org.dataconservancy.packaging.tool.ser.AbstractXstreamTest;
import org.dataconservancy.packaging.tool.ser.ApplicationVersionConverter;
import org.dataconservancy.packaging.tool.ser.DefaultModelFactory;
import org.dataconservancy.packaging.tool.ser.DomainProfileUriListConverter;
import org.dataconservancy.packaging.tool.ser.JenaModelSerializer;
import org.dataconservancy.packaging.tool.ser.PackageMetadataConverter;
import org.dataconservancy.packaging.tool.ser.PackageNameConverter;
import org.dataconservancy.packaging.tool.ser.SerializationAnnotationUtil;
import org.dataconservancy.packaging.tool.ser.StreamMarshaller;
import org.dataconservancy.packaging.tool.ser.UserPropertyConverter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.xstream.XStreamMarshaller;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import java.beans.PropertyDescriptor;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestObjects.applicationVersion;
import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestObjects.domainObjectsRDF;
import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestObjects.domainProfileUris;
import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestObjects.packageMetadata;
import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestObjects.packageName;
import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestObjects.packageTreeRDF;
import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestObjects.userProperties;
import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestResources.APPLICATION_VERSION_1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 *
 */
public class AnnotationDrivenPackageStateSerializerTest {

    /**
     * A mock ArchiveStreamFactory, producing mock ArchiveOutputStream and mock ArchiveEntry objects.
     */
    private ArchiveStreamFactory arxFactory;

    /**
     * A live PackageState object, typically populated by objects in the
     * {@link AbstractXstreamTest.TestObjects} class.
     */
    private PackageState state = new PackageState();

    /**
     * A map of StreamIds to live (not mocked) StreamMarshallers.  When adding or removing entries to this map you
     * probably should also be updating {@link #mockedMarshallerMap}.
     */
    private Map<StreamId, StreamMarshaller> liveMarshallerMap = new HashMap<StreamId, StreamMarshaller>() {
        {
            put(StreamId.APPLICATION_VERSION, new StreamMarshaller() {
                {
                    setStreamId(StreamId.APPLICATION_VERSION);
                    setMarshaller(new XStreamMarshaller());
                    setUnmarshaller(new XStreamMarshaller());
                }
            });
            put(StreamId.PACKAGE_METADATA, new StreamMarshaller() {
                {
                    setStreamId(StreamId.PACKAGE_METADATA);
                    setMarshaller(new XStreamMarshaller());
                    setUnmarshaller(new XStreamMarshaller());
                }
            });
            put(StreamId.PACKAGE_NAME, new StreamMarshaller() {
                {
                    setStreamId(StreamId.PACKAGE_NAME);
                    setMarshaller(new XStreamMarshaller());
                    setUnmarshaller(new XStreamMarshaller());
                }
            });

            final JenaModelSerializer serializer = new JenaModelSerializer(new DefaultModelFactory());
            serializer.setLang("TTL");

            put(StreamId.PACKAGE_TREE, new StreamMarshaller() {
                {
                    setStreamId(StreamId.PACKAGE_TREE);
                    setMarshaller(serializer);
                    setUnmarshaller(serializer);
                }
            });
            put(StreamId.DOMAIN_PROFILE_LIST, new StreamMarshaller() {
                {
                    setStreamId(StreamId.DOMAIN_PROFILE_LIST);
                    setMarshaller(new XStreamMarshaller());
                    setUnmarshaller(new XStreamMarshaller());
                }
            });
            put(StreamId.DOMAIN_OBJECTS, new StreamMarshaller() {
                {
                    setStreamId(StreamId.DOMAIN_OBJECTS);
                    setMarshaller(serializer);
                    setUnmarshaller(serializer);

                }
            });
            put(StreamId.USER_SPECIFIED_PROPERTIES, new StreamMarshaller() {
                {
                    setStreamId(StreamId.USER_SPECIFIED_PROPERTIES);
                    setMarshaller(new XStreamMarshaller());
                    setUnmarshaller(new XStreamMarshaller());
                }
            });
        }
    };

    /**
     * A map of StreamIds to mocked StreamMarshallers.  When adding or removing entries to this map you probably should
     * also be updating {@link #liveMarshallerMap}.
     */
    private Map<StreamId, StreamMarshaller> mockedMarshallerMap = new HashMap<StreamId, StreamMarshaller>() {
        {
            put(StreamId.APPLICATION_VERSION, new StreamMarshaller() {
                {
                    setStreamId(StreamId.APPLICATION_VERSION);
                    setMarshaller(mock(Marshaller.class));
                    setUnmarshaller(mock(Unmarshaller.class));
                }
            });
            put(StreamId.PACKAGE_METADATA, new StreamMarshaller() {
                {
                    setStreamId(StreamId.PACKAGE_METADATA);
                    setMarshaller(mock(Marshaller.class));
                    setUnmarshaller(mock(Unmarshaller.class));
                }
            });
            put(StreamId.PACKAGE_NAME, new StreamMarshaller() {
                {
                    setStreamId(StreamId.PACKAGE_NAME);
                    setMarshaller(mock(Marshaller.class));
                    setUnmarshaller(mock(Unmarshaller.class));
                }
            });
            put(StreamId.PACKAGE_TREE, new StreamMarshaller() {
                {
                    setStreamId(StreamId.PACKAGE_TREE);
                    setUnmarshaller(mock(Unmarshaller.class));
                    setMarshaller(mock(Marshaller.class));
                }
            });
            put(StreamId.DOMAIN_PROFILE_LIST, new StreamMarshaller() {
                {
                    setStreamId(StreamId.DOMAIN_PROFILE_LIST);
                    setUnmarshaller(mock(Unmarshaller.class));
                    setMarshaller(mock(Marshaller.class));
                }
            });
            put(StreamId.DOMAIN_OBJECTS, new StreamMarshaller() {
                {
                    setStreamId(StreamId.DOMAIN_OBJECTS);
                    setUnmarshaller(mock(Unmarshaller.class));
                    setMarshaller(mock(Marshaller.class));
                }
            });
            put(StreamId.USER_SPECIFIED_PROPERTIES, new StreamMarshaller() {
                {
                    setStreamId(StreamId.USER_SPECIFIED_PROPERTIES);
                    setUnmarshaller(mock(Unmarshaller.class));
                    setMarshaller(mock(Marshaller.class));
                }
            });
        }
    };

    /**
     * The instance under test.
     */
    private AnnotationDrivenPackageStateSerializer underTest = new AnnotationDrivenPackageStateSerializer();

    @Before
    public void setUp() throws Exception {

        /*
          A mock ArchiveOutputStream
         */
        ArchiveOutputStream arxOs = mock(ArchiveOutputStream.class);

        /*
         * Mock the classes related to archiving support
         */
        arxFactory = mock(ArchiveStreamFactory.class);
        when(arxFactory.newArchiveOutputStream(any(OutputStream.class))).thenReturn(arxOs);

        /*
         * Populate the live package state object with test objects
         */
        state.setCreationToolVersion(applicationVersion);
        state.setPackageName(packageName);
        state.setPackageMetadataList(packageMetadata);
        state.setDomainProfileIdList(domainProfileUris);
        state.setDomainObjectRDF(domainObjectsRDF);
        state.setUserSpecifiedProperties(userProperties);
        state.setPackageTree(packageTreeRDF);

        /*
         * Configure the live stream marshalling map with XStream converters.  Not all streams are marshalled by
         * XStream.  The PACKAGE_TREE and DOMAIN_OBJECTS streams are RDF, and marshalled by Jena.
         */

        /* Marshallers */
        configure(((XStreamMarshaller) liveMarshallerMap.get(StreamId.APPLICATION_VERSION).getMarshaller()).getXStream());
        configure(((XStreamMarshaller) liveMarshallerMap.get(StreamId.PACKAGE_NAME).getMarshaller()).getXStream());
        configure(((XStreamMarshaller) liveMarshallerMap.get(StreamId.PACKAGE_METADATA).getMarshaller()).getXStream());
        configure(((XStreamMarshaller) liveMarshallerMap.get(StreamId.USER_SPECIFIED_PROPERTIES).getMarshaller()).getXStream());
        configure(((XStreamMarshaller) liveMarshallerMap.get(StreamId.DOMAIN_PROFILE_LIST).getMarshaller()).getXStream());


        /* Unmarshallers */
        configure(((XStreamMarshaller) liveMarshallerMap.get(StreamId.APPLICATION_VERSION).getUnmarshaller()).getXStream());
        configure(((XStreamMarshaller) liveMarshallerMap.get(StreamId.PACKAGE_NAME).getUnmarshaller()).getXStream());
        configure(((XStreamMarshaller) liveMarshallerMap.get(StreamId.PACKAGE_METADATA).getUnmarshaller()).getXStream());
        configure(((XStreamMarshaller) liveMarshallerMap.get(StreamId.USER_SPECIFIED_PROPERTIES).getUnmarshaller()).getXStream());
        configure(((XStreamMarshaller) liveMarshallerMap.get(StreamId.DOMAIN_PROFILE_LIST).getUnmarshaller()).getXStream());

        /*
         * Configure the class under test with the mocked marshaller map, and the mock archive
         * stream factory.  Individual tests can set their marshallers, like using a live marshaller map.
         */
        underTest.setMarshallerMap(mockedMarshallerMap);
        underTest.setArxStreamFactory(arxFactory);
    }

    @Test
    public void testSerializeSimple() throws Exception {
        underTest.setArchive(false);
        underTest.serialize(state, StreamId.APPLICATION_VERSION, new NullOutputStream());

        verify(mockedMarshallerMap.get(StreamId.APPLICATION_VERSION).getMarshaller())
                .marshal(eq(applicationVersion), isNotNull(Result.class));
        verifyZeroInteractions(arxFactory);
    }

    @Test
    public void testDeserializeSimple() throws Exception {
        state = new PackageState();
        when(mockedMarshallerMap.get(StreamId.APPLICATION_VERSION).getUnmarshaller().unmarshal(any(Source.class))).thenReturn(applicationVersion);
        underTest.deserialize(state, StreamId.APPLICATION_VERSION, new BufferedInputStream(APPLICATION_VERSION_1.getInputStream()));
        assertNotNull(state.getCreationToolVersion());
        assertEquals(applicationVersion, state.getCreationToolVersion());
        verify(mockedMarshallerMap.get(StreamId.APPLICATION_VERSION).getUnmarshaller()).unmarshal(any(Source.class));
    }

    @Test
    public void testDeserializeZipArchiveSimple() throws Exception {
        // produce a zip archive containing a single serialized stream for this test.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry zipEntry = new ZipArchiveEntry(StreamId.APPLICATION_VERSION.name());
        zipOut.putArchiveEntry(zipEntry);
        IOUtils.copy(APPLICATION_VERSION_1.getInputStream(), zipOut);
        zipOut.closeArchiveEntry();
        zipOut.close();

        state = new PackageState();
        when(mockedMarshallerMap.get(StreamId.APPLICATION_VERSION).getUnmarshaller().unmarshal(any(Source.class))).thenReturn(applicationVersion);
        ByteArrayInputStream zipIn = new ByteArrayInputStream(baos.toByteArray());
        underTest.deserialize(state, StreamId.APPLICATION_VERSION, zipIn);

        assertNotNull(state.getCreationToolVersion());
        assertEquals(applicationVersion, state.getCreationToolVersion());
        verify(mockedMarshallerMap.get(StreamId.APPLICATION_VERSION).getUnmarshaller()).unmarshal(any(Source.class));
    }

    @Test
    public void testSerializeStreamWithNullFieldInPackageState() throws Exception {
        state = new PackageState();
        StreamResult result = new StreamResult(new NullOutputStream());  // we're using mocks, so nothing will be
        // written to the output stream
        assertNull(state.getCreationToolVersion());

        underTest.serializeToResult(state, StreamId.APPLICATION_VERSION, result);

        // Nothing was serialized, the application version field for the state was null.
        verifyZeroInteractions(mockedMarshallerMap.get(StreamId.APPLICATION_VERSION).getMarshaller());

        // Set a non-null value, and try again
        state.setCreationToolVersion(applicationVersion);
        underTest.serializeToResult(state, StreamId.APPLICATION_VERSION, result);

        verify(mockedMarshallerMap.get(StreamId.APPLICATION_VERSION).getMarshaller()).marshal(applicationVersion, result);
    }

    @Test
    public void testMarshalEntireState() throws Exception {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        underTest.setArchive(false);
        underTest.serialize(state, sink);

        AtomicInteger verifiedStreamCount = new AtomicInteger(0);
        mockedMarshallerMap.entrySet().forEach(entry -> {
            StreamId streamId = entry.getKey();
            StreamMarshaller streamMarshaller = entry.getValue();

            try {
                switch (streamId) {

                    case APPLICATION_VERSION:
                        verify(streamMarshaller.getMarshaller())
                                .marshal(eq(applicationVersion), isNotNull(Result.class));
                        verifiedStreamCount.incrementAndGet();
                        break;

                    case PACKAGE_NAME:
                        verify(streamMarshaller.getMarshaller())
                                .marshal(eq(packageName), isNotNull(Result.class));
                        verifiedStreamCount.incrementAndGet();
                        break;

                    case PACKAGE_METADATA:
                        verify(streamMarshaller.getMarshaller())
                                .marshal(eq(packageMetadata), isNotNull(Result.class));
                        verifiedStreamCount.incrementAndGet();
                        break;

                    case DOMAIN_PROFILE_LIST:
                        verify(streamMarshaller.getMarshaller())
                                .marshal(eq(domainProfileUris), isNotNull(Result.class));
                        verifiedStreamCount.incrementAndGet();
                        break;

                    case DOMAIN_OBJECTS:
                        verify(streamMarshaller.getMarshaller())
                                .marshal(eq(domainObjectsRDF), isNotNull(Result.class));
                        verifiedStreamCount.incrementAndGet();
                        break;

                    case USER_SPECIFIED_PROPERTIES:
                        verify(streamMarshaller.getMarshaller())
                                .marshal(eq(userProperties), isNotNull(Result.class));
                        verifiedStreamCount.incrementAndGet();
                        break;

                    case PACKAGE_TREE:
                        verify(streamMarshaller.getMarshaller())
                                .marshal(eq(packageTreeRDF), isNotNull(Result.class));
                        verifiedStreamCount.incrementAndGet();
                        break;
                }
            } catch (IOException e) {
                fail("Encountered IOE: " + e.getMessage());
            }
        });

        assertEquals(mockedMarshallerMap.size(), verifiedStreamCount.intValue());
    }

    @Test
    public void testUnmarshalEntireState() throws Exception {
        // First, create a state file to unmarshal
        // 1. It must be a zip archive; unmarshalling a non-archive package state file isn't supported
        // 2. We use the liveMarshallerMap; no mocks.
        // 3. We use a live ArchiveStreamFactory
        underTest.setArchive(true);
        underTest.setMarshallerMap(liveMarshallerMap);
        underTest.setArxStreamFactory(new ZipArchiveStreamFactory());
        File tmp = File.createTempFile(this.getClass().getName() + "_UnmarshalEntireState", ".zip");
        OutputStream out = new FileOutputStream(tmp);
        underTest.serialize(state, out);
        assertTrue(tmp.exists() && tmp.length() > 1);

        // Verify that we wrote out a zip file.
        final byte[] signature = new byte[12];
        InputStream in = new BufferedInputStream(new FileInputStream(tmp));
        in.mark(signature.length);
        int bytesRead = org.apache.commons.compress.utils.IOUtils.readFully(in, signature);
        assertTrue(ZipArchiveInputStream.matches(signature, bytesRead));
        in.reset();

        // Create a new instance of PackageState, and deserialize the zip archive created above, which contains the
        // test objects from the {@link #state prepared instance} of PackageState
        PackageState state = new PackageState();  // a new instance of PackageState with no internal state
        underTest.deserialize(state, in);

        Map<StreamId, PropertyDescriptor> pds = SerializationAnnotationUtil.getStreamDescriptors(PackageState.class);
        assertTrue(pds.size() > 0);
        pds.keySet().stream().forEach(streamId -> {
            try {
                assertNotNull("Expected non-null value for PackageState field '" + pds.get(streamId).getName() + "', " +
                                "StreamId '" + streamId + "'",
                        pds.get(streamId).getReadMethod().invoke(state));
            } catch (IllegalAccessException | InvocationTargetException e) {
                fail(e.getMessage());
            }
        });

        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
        FileUtils.deleteQuietly(tmp);
    }

    @Test
    public void testSimpleArchive() throws Exception {
        underTest.setArchive(true);

        underTest.serialize(state, StreamId.APPLICATION_VERSION, new NullOutputStream());

        verify(mockedMarshallerMap.get(StreamId.APPLICATION_VERSION).getMarshaller())
                .marshal(eq(applicationVersion), isNotNull(Result.class));
        verify(arxFactory).newArchiveOutputStream(any(OutputStream.class));
//        verify(arxFactory).newArchiveEntry(eq(StreamId.APPLICATION_VERSION.name()), any(), any(), any(), eq(0644));
    }

    @Test
    public void testSerializeApplicationVersionWithLiveMarshallers() throws Exception {
        underTest.setArchive(false);
        underTest.setMarshallerMap(liveMarshallerMap);

        // Set a spy on the ApplicationVersionConverter
        ApplicationVersionConverter applicationVersionConverter = spy(new ApplicationVersionConverter());
        XStreamMarshaller xsm = (XStreamMarshaller) underTest.getMarshallerMap()
                .get(StreamId.APPLICATION_VERSION).getMarshaller();
        XStream x = xsm.getXStream();
        x.registerConverter(applicationVersionConverter, XStream.PRIORITY_VERY_HIGH);  // because there is a non-spy
                                                                                       // version of this already
                                                                                       // registered in the configure
                                                                                       // method

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        underTest.serialize(state, StreamId.APPLICATION_VERSION, result);

        verify(applicationVersionConverter, atLeastOnce()).canConvert(ApplicationVersion.class);
        // cant verify the marshal(...) method b/c it's final
        verify(applicationVersionConverter).
                marshalInternal(eq(applicationVersion),
                        any(HierarchicalStreamWriter.class), any(MarshallingContext.class));
        assertTrue(result.size() > 1);
    }

    @Test
    public void testArchiveSerializeApplicationVersion() throws Exception {
        underTest.setArchive(true);
        underTest.setMarshallerMap(liveMarshallerMap);

        // Set a spy on the ApplicationVersionConverter
        ApplicationVersionConverter applicationVersionConverter = spy(new ApplicationVersionConverter());
        XStreamMarshaller xsm = (XStreamMarshaller) underTest.getMarshallerMap()
                .get(StreamId.APPLICATION_VERSION).getMarshaller();
        XStream x = xsm.getXStream();
        x.registerConverter(applicationVersionConverter, XStream.PRIORITY_VERY_HIGH);  // because there is a non-spy
                                                                                       // version of this already
                                                                                       // registered in the configure
                                                                                       // method

        ByteArrayOutputStream result = new ByteArrayOutputStream();

        when(arxFactory.newArchiveOutputStream(result)).thenAnswer(invocationOnMock ->
                new ArchiveOutputStream() {
                    @Override
                    public void putArchiveEntry(ArchiveEntry archiveEntry) throws IOException {
                    }

                    @Override
                    public void closeArchiveEntry() throws IOException {
                    }

                    @Override
                    public void finish() throws IOException {
                    }

                    @Override
                    public ArchiveEntry createArchiveEntry(File file, String s) throws IOException {
                        return mock(ArchiveEntry.class);
                    }

                    @Override
                    public void write(byte[] b, int off, int len) throws IOException {
                        result.write(b, off, len);
                    }
                });


        underTest.serialize(state, StreamId.APPLICATION_VERSION, result);

        verify(applicationVersionConverter, atLeastOnce()).canConvert(ApplicationVersion.class);
        // cant verify the marshal(...) method b/c it's final
        verify(applicationVersionConverter).
                marshalInternal(eq(applicationVersion),
                        any(HierarchicalStreamWriter.class), any(MarshallingContext.class));
        assertTrue(result.size() > 1);
    }

    @Test
    public void testArchiveZipLive() throws Exception {
        underTest.setArchive(true);
        underTest.setArxStreamFactory(new ZipArchiveStreamFactory());

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        underTest.serialize(state, StreamId.APPLICATION_VERSION, result);

        assertTrue(result.size() > 1);
    }

    @Test
    public void testDeserializeEmptyArchive() throws Exception {
        PackageState state = new PackageState();
        underTest.setMarshallerMap(liveMarshallerMap);
        BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(ZipLong.LFH_SIG.getBytes()));

        underTest.deserialize(state, in);

        assertEmptyOrNullStreamsOnPackageState(state);
    }

    @Test
    public void testDeserializeEmptyStream() throws Exception {
        ClassPathResource stateWithEmptyStream = new ClassPathResource("/stateWithEmptyStream.zip");
        BufferedInputStream in = new BufferedInputStream(stateWithEmptyStream.getInputStream());

        PackageState state = new PackageState();
        underTest.setMarshallerMap(liveMarshallerMap);

        underTest.deserialize(state, in);
        assertEmptyOrNullStreamsOnPackageState(state, StreamId.USER_SPECIFIED_PROPERTIES);
    }

    /**
     * Asserts that all @Serialize annotated fields on the supplied state have a null value.  If an annotated field
     * is a Collection or a Map, they are allowed to be not null, but they must be empty.
     *
     * @param state the state object
     */
    private void assertEmptyOrNullStreamsOnPackageState(PackageState state) {
        assertEmptyOrNullStreamsOnPackageState(state, null);
    }

    /**
     * Asserts that specified streams on the supplied state have a null value.  If a stream
     * is a Collection or a Map, they are allowed to be not null, but they must be empty.
     *
     * @param state the state object
     */
    private void assertEmptyOrNullStreamsOnPackageState(PackageState state, StreamId... streamIds) {
        boolean all = (streamIds == null || streamIds.length == 0);

        SerializationAnnotationUtil.getStreamDescriptors(PackageState.class).forEach((streamId, pd) -> {
            if (all || Stream.of(streamIds).anyMatch(candidate -> candidate == streamId)) {
                Class<?> type = pd.getPropertyType();
                try {
                    Object fieldValue = pd.getReadMethod().invoke(state);
                    if (Collection.class.isAssignableFrom(type) && fieldValue != null) {
                        assertTrue("Expected empty collection for streamId " + streamId + ", field " + pd.getName() +
                                " on " + PackageState.class.getName(), ((Collection) fieldValue).isEmpty());
                    } else if (Map.class.isAssignableFrom(type) && fieldValue != null) {
                        assertTrue("Expected empty collection for streamId " + streamId + ", field " + pd.getName() +
                                " on " + PackageState.class.getName(), ((Map) fieldValue).isEmpty());
                    } else {
                        assertNull("Expected 'null' value for streamId " + streamId + ", field " + pd.getName() + " on " +
                                PackageState.class.getName(), fieldValue);
                    }
                } catch (Exception e) {
                    fail(e.getMessage());
                }
            }
        });
    }

    private XStream configure(XStream x) {
        x.processAnnotations(PackageState.class);

        /* APPLICATION_VERSION */
        x.alias(StreamId.APPLICATION_VERSION.name(),
                AbstractSerializationTest.TestObjects.applicationVersion.getClass());
        PropertyDescriptor pd = SerializationAnnotationUtil.getStreamDescriptors(PackageState.class)
                .get(StreamId.APPLICATION_VERSION);
        x.registerLocalConverter(PackageState.class, pd.getName(), new ApplicationVersionConverter());

        /* DOMAIN_PROFILE_LIST */
        x.alias(StreamId.DOMAIN_PROFILE_LIST.name(),
                 AbstractSerializationTest.TestObjects.domainProfileUris.getClass());
        pd = SerializationAnnotationUtil.getStreamDescriptors(PackageState.class).get(StreamId.DOMAIN_PROFILE_LIST);
        x.registerLocalConverter(PackageState.class, pd.getName(), new DomainProfileUriListConverter());

        /* PACKAGE_METADATA */
        x.alias(StreamId.PACKAGE_METADATA.name(),
                 AbstractSerializationTest.TestObjects.packageMetadata.getClass());
        pd = SerializationAnnotationUtil.getStreamDescriptors(PackageState.class).get(StreamId.PACKAGE_METADATA);
        x.registerLocalConverter(PackageState.class, pd.getName(), new PackageMetadataConverter());

        /* PACKAGE_NAME */
        x.alias(StreamId.PACKAGE_NAME.name(),
                 AbstractSerializationTest.TestObjects.packageName.getClass());
        pd = SerializationAnnotationUtil.getStreamDescriptors(PackageState.class).get(StreamId.PACKAGE_NAME);
        x.registerLocalConverter(PackageState.class, pd.getName(), new PackageNameConverter());

        /* USER_SPECIFIED_PROPERTIES */
        x.alias(StreamId.USER_SPECIFIED_PROPERTIES.name(),
                 AbstractSerializationTest.TestObjects.userProperties.getClass());
        pd = SerializationAnnotationUtil.getStreamDescriptors(PackageState.class)
                .get(StreamId.USER_SPECIFIED_PROPERTIES);
        x.registerLocalConverter(PackageState.class, pd.getName(), new UserPropertyConverter());

        return x;
    }
    
}