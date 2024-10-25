import com.jmfg.consumer.db.ProductCreatedEventRepository
import com.jmfg.consumer.handler.ProductCreatedEventHandler
import com.jmfg.core.Product
import com.jmfg.core.ProductCreatedEvent
import com.jmfg.core.RetryableException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@ExtendWith(SpringExtension::class)
@SpringBootTest
class ProductCreatedEventHandlerTest {

    @Autowired
    private lateinit var productCreatedEventHandler: ProductCreatedEventHandler

    @MockBean
    private lateinit var webClient: WebClient

    @MockBean
    private lateinit var productRepository: ProductCreatedEventRepository

    @Test
    fun `handle should save product to database on successful retrieve`() {
        val product =
            Product(id = "1", name = "Test Product", description = "Test Description", price = 10.0, quantity = 5)
        val event = ProductCreatedEvent(id = "1", product = product)

        val webClientResponse = mock(WebClient.ResponseSpec::class.java)
        `when`(webClient.get().uri("/products/1").retrieve()).thenReturn(webClientResponse)
        `when`(webClientResponse.bodyToMono(Product::class.java)).thenReturn(Mono.just(product))

        productCreatedEventHandler.handle(event, event.id)

        verify(productRepository, times(1)).save(event)
    }

    @Test
    fun `handle should throw RetryableException on error`() {
        val event = ProductCreatedEvent(id = "1")

        val webClientResponse = mock(WebClient.ResponseSpec::class.java)
        `when`(webClient.get().uri("/products/1").retrieve()).thenReturn(webClientResponse)
        `when`(webClientResponse.bodyToMono(Product::class.java)).thenReturn(Mono.error(RuntimeException("Error")))

        assertThrows(RetryableException::class.java) {
            productCreatedEventHandler.handle(event, event.id)
        }
    }
}