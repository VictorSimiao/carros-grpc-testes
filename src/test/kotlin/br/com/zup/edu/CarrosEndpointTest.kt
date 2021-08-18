package br.com.zup.edu

import io.grpc.Channel
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CarrosEndpointTest(
    val repository: CarroRepository,
    val grpcClient: CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub
) {

    @BeforeEach
    fun setup(){
        repository.deleteAll()
    }

    @Test
    fun `deve cadastrar um novo carro`() {

        val response = grpcClient.adicionar(
            CarroRequest.newBuilder()
                .setModelo("Gol")
                .setPlaca("MTY-3467")
                .build()
        )

        //validação
        assertNotNull(response.id)
        assertTrue(repository.existsById(response.id))//efeito colateral

    }

    @Test
    fun `nao deve cadastrar um novo carro com placa já existente`() {

        val carroExistente = repository.save(Carro(modelo = "Palio", placa = "MJK-8989"))

        //ação

       val erro = assertThrows<StatusRuntimeException> {
            grpcClient.adicionar(
                CarroRequest.newBuilder()
                    .setModelo("Ferrari")
                    .setPlaca(carroExistente.placa)
                    .build()
            )
        }
        //Validação
        assertEquals(Status.ALREADY_EXISTS.code, erro.status.code )
        assertEquals("Carro com placa existente", erro.status.description)
    }

    @Test
    fun `nao deve cadastrar novo carro quando dados de entrada forem invalidos`(){
        val carroExistente = repository.save(Carro(modelo = "Palio", placa = "MJK-8989"))

        //ação

        val erro = assertThrows<StatusRuntimeException> {
            grpcClient.adicionar(
                CarroRequest.newBuilder()
                    .setModelo("")
                    .setPlaca("")
                    .build()
            )
        }
        //Validação
        assertEquals(Status.INVALID_ARGUMENT.code, erro.status.code )
        assertEquals("dados de entrada inválidos", erro.status.description)
    }


    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub? {
            return CarrosGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}