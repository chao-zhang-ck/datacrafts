package org.datacrafts.noschema.operator

import org.datacrafts.logging.Slf4jLogging
import org.datacrafts.noschema.{Operation, ShapelessProduct}
import org.datacrafts.noschema.Operation.Operator
import org.datacrafts.noschema.ShapelessProduct.{SymbolCollector, SymbolExtractor}
import org.datacrafts.noschema.operator.ShapelessProductOperator.ProductBuilder

abstract class ShapelessProductOperator[T, I, O] extends Operator[T] with Slf4jLogging.Default {

  protected def shapeless: ShapelessProduct[T, _]

  protected def parse(input: I): SymbolExtractor

  protected def newProductBuilder(): ProductBuilder[O]

  protected final override def marshalNoneNull(input: Any): T = {
    val className = input.getClass.getCanonicalName
    if (className == operation.context.noSchema.scalaType.tpe.toString) {
      logDebug(s"input is already expected type: ${operation.context.noSchema.scalaType}")
      input.asInstanceOf[T]
    } else {
      logDebug(s"input ${input}[${className}] is not expected type: " +
        s"${operation.context.noSchema.scalaType.tpe}, parse and perform shapeless transform")
      shapeless.marshal(parse(input.asInstanceOf[I]), operation)
    }
  }

  protected final override def unmarshalNoneNull(input: T): O = {
    shapeless.unmarshal(input, newProductBuilder(), operation)
      .asInstanceOf[ProductBuilder[O]].build()
  }
}

object ShapelessProductOperator {

  trait ProductBuilder[O] extends SymbolCollector {
    def build(): O
  }

}
