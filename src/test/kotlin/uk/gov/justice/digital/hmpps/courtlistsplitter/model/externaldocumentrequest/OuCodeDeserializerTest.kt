package uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.io.IOException

@ExtendWith(MockitoExtension::class)
internal class OuCodeDeserializerTest {
  @Mock
  private lateinit var jsonNode: JsonNode

  @Mock
  private lateinit var jsonParser: JsonParser

  @Mock
  private lateinit var objectCodec: ObjectCodec

  @Mock
  private lateinit var context: DeserializationContext

  private lateinit var deserializer: OuCodeDeserializer<String>

  @BeforeEach
  fun beforeEach() {
    deserializer = OuCodeDeserializer(String::class.java)
  }

  @Test
  @Throws(IOException::class)
  fun `when element populated then truncate the OU code`() {

    whenever(jsonParser.codec).thenReturn(objectCodec)
    whenever(objectCodec.readTree<TreeNode>(jsonParser)).thenReturn(jsonNode)
    whenever(jsonNode.asText("")).thenReturn("B01OB00")
    val ouCode: String = deserializer.deserialize(jsonParser, context)
    assertThat(ouCode).isEqualTo("B01OB")
  }
}
