package scala.meta.serialiser

import org.scalatest._
import shapeless.test.illTyped

class MappableTest extends WordSpec with Matchers {

  @mappable case class SimpleCaseClass(i: Int, s: String)
  "simple case class" should {
    "serialise and deserialise" in {
      val testInstance = SimpleCaseClass(i = 42, s = "something")
      val keyValues = testInstance.toMap
      SimpleCaseClass.fromMap(keyValues) shouldBe Some(testInstance)
    }
  }

  @mappable case class WithTypeParam[N <: Number](n: Number)
  "case class with type param" should {
    "serialise and deserialise" in {
      val testInstance = WithTypeParam[Integer](n = 43)
      val keyValues = testInstance.toMap
      WithTypeParam.fromMap[Integer](keyValues) shouldBe Some(testInstance)
    }
  }

  @mappable case class WithBody(i: Int) { def banana: Int = i }
  "case class with body" should {
    "still have the body as before" in {
      WithBody(100).banana shouldBe 100
    }
  }

  @mappable case class WithOption(i: Int, s: Option[String])
  "case class with Option member" should {
    "serialise and deserialise `None`" in {
      val testInstance = WithOption(i = 42, s = None)
      val keyValues = testInstance.toMap
      keyValues shouldBe Map("i" -> 42, "s" -> null)
      WithOption.fromMap(keyValues) shouldBe Some(testInstance)
    }

    "serialise and deserialise `Some`" in {
      val testInstance = WithOption(i = 42, s = Some("thing"))
      val keyValues = testInstance.toMap
      keyValues shouldBe Map("i" -> 42, "s" -> "thing")
      WithOption.fromMap(keyValues) shouldBe Some(testInstance)
    }
  }

  object WithCompanion { def existingFun(): Int = 42 }
  @mappable case class WithCompanion (i: Int, s: String)
  "case class with companion" should {
    "serialise and deserialise" in {
      val testInstance = WithCompanion(i = 42, s = "something")
      val keyValues = testInstance.toMap
      WithCompanion.fromMap(keyValues) shouldBe Some(testInstance)
    }

    "keep existing functionality in companion" in {
      WithCompanion.existingFun shouldBe 42
    }
  }

  @mappable case class WithNullable(someValue: String, @nullable nullableValue: String)
  "deserialising null values" should {
    "error by default" in {
      val keyValues = Map("nullableValue" -> "something") // someValue not set
      WithNullable.fromMap(keyValues) shouldBe None
    }

    "set @nullable members to Null" in {
      val keyValues = Map("someValue" -> "something") // nullableValue not set
      WithNullable.fromMap(keyValues) shouldBe Some(WithNullable(someValue = "something", nullableValue = null))
    }
  }

  @mappable case class WithDefaultValue(i: Int = 13, s: String)
  "case class with default" should {
    "serialise and deserialise" in {
      val testInstance = WithDefaultValue(s = "something")
      val keyValue = testInstance.toMap
      WithDefaultValue.fromMap(keyValue) shouldBe Some(testInstance)
    }

    "store correct defaultValueMap" in {
      WithDefaultValue.defaultValueMap shouldBe (Map[String, Any]("i" -> 13))
    }

    "keep default value in fromMap" in {
      val testInstance = WithDefaultValue(s = "something") // with default i = 13
      val keyValue = Map[String, Any]("s" -> "something")
      WithDefaultValue.fromMap(keyValue) shouldBe Some(testInstance)
    }
  }

  @mappable case class WithCustomMapping(
    @mappedTo("iMapped") i: Int,
    @mappedTo("jMapped") j: Option[Int],
                         s: String)
  "when defining custom mappings" should {
    "serialise and deserialise" in {
      val testInstance = WithCustomMapping(i = 42, j = Some(43), s = "something")
      val keyValues = testInstance.toMap
      keyValues shouldBe Map("iMapped" -> 42, "jMapped" -> 43, "s" -> "something")
      WithCustomMapping.fromMap(keyValues) shouldBe Some(testInstance)
    }
  }

  @mappable(Map("param1" -> "paramValue1"))
  case class WithAnnotationParam(i: Int)
  "makes annotation parameters available in companion object" in {
    WithAnnotationParam.params shouldBe Map("param1" -> "paramValue1")
  }

  "fromMap" should {
    "return None if provided with invalid data" in {
      val invalidKeyValues = Map("in" -> "valid")

      // not necessarily a complete list
      Seq(
        SimpleCaseClass.fromMap,
        WithTypeParam.fromMap[Integer],
        WithBody.fromMap,
        WithCompanion.fromMap) foreach { fromMap: FromMap[_] =>
          fromMap(invalidKeyValues) shouldBe None
      }
    }
  }

  /* generated code will be printed out on the console */
  @mappable(Map("_debug" -> "true"))
  case class WithDebugEnabled(i: Int)
}
