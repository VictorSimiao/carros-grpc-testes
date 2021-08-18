package br.com.zup.edu

import io.grpc.Channel
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CarrosEndpointTest(val repository:CarroRepository, val grpcClient:CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub){

    @Test
    fun `deve cadastrar um novo carro`(){
        repository.deleteAll()
        //ação
        val response = grpcClient.adicionar(CarroRequest.newBuilder()
            .setModelo("Gol")
            .setPlaca("MTY-3467")
            .build())

        //validação
        assertNotNull(response.id)
        assertTrue(repository.existsById(response.id))//efeito colateral
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel:ManagedChannel):CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub?{
            return CarrosGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}