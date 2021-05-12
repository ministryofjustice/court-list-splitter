package uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.io.IOException
import java.time.LocalDate
import java.time.Month

@ExtendWith(MockitoExtension::class)
internal class DocumentInfoDeserializerTest {
  @Mock
  private lateinit var jsonNode: JsonNode

  @Mock
  private lateinit var sourceFileNameNode: JsonNode

  @Mock
  private lateinit var jsonParser: JsonParser

  @Mock
  private lateinit var objectCodec: ObjectCodec

  @Mock
  private lateinit var context: DeserializationContext

  private lateinit var deserializer: DocumentInfoDeserializer<Info>

  @BeforeEach
  fun beforeEach() {
    deserializer = DocumentInfoDeserializer<Info>(Info::class.java)
  }

  @Test
  @Throws(IOException::class)
  fun `when element is populated with a normal string then Info is created`() {
    whenever(jsonParser.codec).thenReturn(objectCodec)
    whenever(objectCodec.readTree<TreeNode>(jsonParser)).thenReturn(jsonNode)
    whenever(jsonNode[Info.SOURCE_FILE_NAME_ELEMENT]).thenReturn(sourceFileNameNode)
    whenever(sourceFileNameNode.asText("")).thenReturn("146_27072020_2578_B01OB00_ADULT_COURT_LIST_DAILY")

    val info = deserializer.deserialize(jsonParser, context)

    assertThat(info.ouCode).isEqualTo("B01OB")
    assertThat(info.dateOfHearing).isEqualTo(LocalDate.of(2020, Month.JULY, 27))
    assertThat(info.sequence).isEqualTo(146)
  }
}
