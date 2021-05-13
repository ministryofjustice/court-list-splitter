package uk.gov.justice.digital.hmpps.courtlistsplitter.messaging

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.MessageHeader
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.MessageID
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.MessageType
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Address
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Block
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Case
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Document
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.DocumentWrapper
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.ExternalDocumentRequest
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Name
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Offence
import uk.gov.justice.digital.hmpps.courtlistsplitter.model.externaldocumentrequest.Session
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.util.function.Predicate
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import javax.validation.Validation

@ExtendWith(SpringExtension::class)
class MessageParserTest {

  @Import(TestMessagingConfig::class)
  @Nested
  @DisplayName("GatewayMessage Parser Test")
  inner class GatewayMessageParser {

    @Autowired
    private lateinit var gatewayMessageParser: MessageParser<MessageType>

    @Test
    @Throws(IOException::class)
    fun `given various sorts of field level validation error then fail`() {
      val content = Files.readString(Paths.get("src/test/resources/messages/gateway-message-invalid.xml"))
      val thrown = catchThrowable {
        gatewayMessageParser.parseMessage(content, MessageType::class.java)
      }
      val ex = thrown as ConstraintViolationException
      assertThat(ex.constraintViolations).hasSize(4)
      val docInfoPath = "messageBody.gatewayOperationType.externalDocumentRequest.documentWrapper.document[0].info"
      val firstSessionPath = "messageBody.gatewayOperationType.externalDocumentRequest.documentWrapper.document[0].data.job.sessions[0]"
      val firstCasePath = "$firstSessionPath.blocks[0].cases[0]"
      val firstOffencePath = "$firstCasePath.offences[0]"
      assertThat(ex.constraintViolations).anyMatch { cv: ConstraintViolation<*> ->
        cv.message == "Invalid ou code" && cv.propertyPath.toString() == "$docInfoPath.ouCode"
      }
      assertThat(ex.constraintViolations).anyMatch { cv: ConstraintViolation<*> ->
        cv.message == "must not be blank" && cv.propertyPath.toString() == "$docInfoPath.ouCode"
      }
      assertThat(ex.constraintViolations).anyMatch { cv: ConstraintViolation<*> ->
        cv.message == "must not be blank" && cv.propertyPath.toString() == "$firstCasePath.caseNo"
      }
      assertThat(ex.constraintViolations).anyMatch { cv: ConstraintViolation<*> ->
        cv.message == "must not be null" && cv.propertyPath.toString() == "$firstOffencePath.seq"
      }
    }

    @Test
    fun `given invalid XML then error`() {
      val thrown = catchThrowable {
        gatewayMessageParser.parseMessage("<msg>sss</msg>", MessageType::class.java)
      }
      val ex = thrown as ConstraintViolationException
      assertThat(ex.constraintViolations).hasSize(2)
    }

    @Test
    @Throws(IOException::class)
    fun `parse a valid message`() {
      val path = "src/test/resources/messages/gateway-message-multi-session.xml"
      val content = Files.readString(Paths.get(path))
      val message = gatewayMessageParser.parseMessage(content, MessageType::class.java)

      val expectedHeader = MessageHeader(
        from = "CP_NPS_ML",
        to = "CP_NPS",
        messageType = "externalDocument",
        timeStamp = "2020-05-29T09:16:40.594Z",
        messageID = MessageID("6be22d98-a8f6-4b2a-b9e7-ca8735037c68", "relatesTo")
      )

      assertThat(message.messageHeader).usingRecursiveComparison()
        .ignoringFields("messageID")
        .isEqualTo(expectedHeader)
      assertThat(message.messageHeader?.messageID).usingRecursiveComparison()
        .isEqualTo(MessageID("6be22d98-a8f6-4b2a-b9e7-ca8735037c68", "relatesTo"))

      val document = message.messageBody?.gatewayOperationType?.externalDocumentRequest?.documentWrapper?.let { getDocumentForOuCode(it, "B01CX") }

      assertThat(document?.data?.job?.sessions).hasSize(1)
      document?.data?.job?.sessions?.stream()?.findFirst()?.orElseThrow()?.let { checkSession(it) }
    }
  }

  @Import(TestMessagingConfig::class)
  @Nested
  @DisplayName("External Document Parser Test")
  inner class ExternalDocumentMessageParser {
    @Autowired
    private lateinit var messageParser: MessageParser<ExternalDocumentRequest>

    @Test
    @Throws(IOException::class)
    fun `parse ExternalDocumentRequest with multiple sessions`() {
      val content = Files.readString(Paths.get("src/test/resources/messages/external-document-request-multi-session.xml"))
      val message: ExternalDocumentRequest = messageParser.parseMessage(content, ExternalDocumentRequest::class.java)

      val document = getDocumentForOuCode(message.documentWrapper, "B01CX")
      assertThat(document.data.job.sessions).hasSize(1)
      document.data.job.sessions.stream().findFirst().orElseThrow().let { checkSession(it) }

      val document2 = getDocumentForOuCode(message.documentWrapper, "B01CY")
      val session: Session = document2.data.job.sessions[0]
      assertThat(session.courtCode).isEqualTo("B01CY")
    }

    @Test
    @Throws(IOException::class)
    fun `when invalid message is received then raise ConstraintViolations`() {
      val content = Files.readString(Paths.get("src/test/resources/messages/external-document-request-invalid.xml"))
      val thrown = catchThrowable {
        messageParser.parseMessage(content, ExternalDocumentRequest::class.java)
      }
      val ex = thrown as ConstraintViolationException
      assertThat(ex.constraintViolations).hasSize(3)
      val docInfoPath = "documentWrapper.document[0].info"
      val firstSessionPath = "documentWrapper.document[0].data.job.sessions[0]"
      assertThat(ex.constraintViolations).anyMatch { cv: ConstraintViolation<*> ->
        cv.message == "must not be blank" && cv.propertyPath.toString() == "$docInfoPath.ouCode"
      }
      assertThat(ex.constraintViolations).anyMatch { cv: ConstraintViolation<*> ->
        cv.message == "Invalid ou code" && cv.propertyPath.toString() == "$docInfoPath.ouCode"
      }
      assertThat(ex.constraintViolations).anyMatch { cv: ConstraintViolation<*> ->
        cv.message == "must not be blank" && cv.propertyPath.toString() == "$firstSessionPath.blocks[0].cases[0].caseNo"
      }
    }
  }

  @TestConfiguration
  class TestMessagingConfig {

    @Bean
    fun gatewayMessageParser(): MessageParser<MessageType> {
      val xmlModule = JacksonXmlModule()
      xmlModule.setDefaultUseWrapper(false)
      val mapper = XmlMapper(xmlModule)
      mapper.registerModule(JavaTimeModule())
      mapper.registerKotlinModule()
      mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      val factory = Validation.buildDefaultValidatorFactory()
      return MessageParser(mapper, factory.validator)
    }

    @Bean
    fun messageParser(): MessageParser<ExternalDocumentRequest> {
      val xmlModule = JacksonXmlModule()
      xmlModule.setDefaultUseWrapper(false)
      val mapper = XmlMapper(xmlModule)
      mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      mapper.registerModule(JavaTimeModule())
      mapper.registerKotlinModule()
      val factory = Validation.buildDefaultValidatorFactory()
      return MessageParser(mapper, factory.validator)
    }
  }

  private fun checkSession(session: Session) {
    assertThat(session.id).isEqualTo(556805L)
    assertThat(session.dateOfHearing).isEqualTo(HEARING_DATE)
    assertThat(session.courtCode).isEqualTo("B01CX")
    assertThat(session.courtName).isEqualTo("Camberwell Green")
    assertThat(session.courtRoom).isEqualTo("00")
    assertThat(session.start).isEqualTo(START_TIME)
    assertThat(session.getSessionStartTime()).isEqualTo(SESSION_START_TIME)
    assertThat(session.end).isEqualTo(LocalTime.of(13, 5))
    assertThat(session.blocks).hasSize(1)
    session.blocks.stream().findFirst().orElseThrow().let { checkBlock(it) }
  }

  private fun checkBlock(block: Block) {
    assertThat(block.cases).hasSize(2)
    block.cases.stream().filter { aCase -> aCase.caseNo.equals("1600032953") }
      ?.findFirst()?.orElseThrow()?.let { checkCase(it) }
  }

  private fun checkCase(case: Case) {
    assertThat(case.defendantAge).isEqualTo(20)
    assertThat(case.caseId).isEqualTo(1217464)
    assertThat(case.defendantName).isEqualTo("Mr. David DLONE")
    assertThat(case.name).isEqualTo(Name(title = "Mr.", forename1 = "David", surname = "DLONE"))
    assertThat(case.defendantType).isEqualTo("P")
    assertThat(case.defendantSex).isEqualTo("N")
    assertThat(case.pnc).isEqualTo("PNC-ID1")
    assertThat(case.cro).isEqualTo("11111/79J")
    assertThat(case.defendantAddress).usingRecursiveComparison().isEqualTo(Address(line1 = "39 The Street", line2 = "Newtown", pcode = "NT4 6YH"))
    assertThat(case.defendantDob).isEqualTo(LocalDate.of(2002, Month.FEBRUARY, 2))
    assertThat(case.nationality1).isEqualTo("Angolan")
    assertThat(case.nationality2).isEqualTo("Austrian")
    assertThat(case.seq).isEqualTo(1)
    assertThat(case.listNo).isEqualTo("1st")
    assertThat(case.offences).hasSize(1)

    case.offences.stream().findFirst().orElseThrow().let { checkOffence(it) }

    assertThat(case.name?.getForenames()).isEqualTo("David")
    assertThat(case.name?.getFullName()).isEqualTo("Mr. David DLONE")
  }

  private fun checkOffence(offence: Offence) {
    assertThat(offence.seq).isEqualTo(1)
    assertThat(offence.title).isEqualTo("Theft from a shop")
    assertThat(offence.summary).isEqualTo("On 01/01/2016 at Town, stole Article, to the value of £100.00, belonging to Person.")
    assertThat(offence.act).isEqualTo("Contrary to section 1(1) and 7 of the Theft Act 1968.")
  }

  private fun getDocumentForOuCode(documentWrapper: DocumentWrapper, ouCode: String): Document {
    val documents: List<Document> = ArrayList(documentWrapper.document)
    assertThat(documents).hasSize(2)
    return documents.stream()
      .filter(
        Predicate<Document> { doc: Document ->
          doc.info.ouCode == ouCode
        }
      )
      .findFirst().orElseThrow()
  }

  companion object {
    val HEARING_DATE: LocalDate = LocalDate.of(2020, Month.FEBRUARY, 20)
    val START_TIME: LocalTime = LocalTime.of(9, 1)
    val SESSION_START_TIME: LocalDateTime = LocalDateTime.of(HEARING_DATE, START_TIME)
  }
}
