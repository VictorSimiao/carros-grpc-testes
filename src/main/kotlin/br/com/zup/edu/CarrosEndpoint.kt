package br.com.zup.edu

import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class CarrosEndpoint(@Inject val repository: CarroRepository) : CarrosGrpcServiceGrpc.CarrosGrpcServiceImplBase() {

    override fun adicionar(request: CarroRequest?, responseObserver: StreamObserver<CarroResponse>?) {
        if (repository.existsByPlaca(request!!.placa)) {
            responseObserver!!.onError(
                Status.ALREADY_EXISTS
                    .withDescription("Carro com placa existente")
                    .asRuntimeException()
            )
            return
        }
        val novoCarro = Carro(request.modelo, request.placa)

        try {
            repository.save(novoCarro)
        } catch (e: ConstraintViolationException) {
            responseObserver!!.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("dados de entrada inválidos")
                    .asRuntimeException()
            )
            return
        }
        responseObserver!!.onNext(
            CarroResponse.newBuilder()
                .setId(novoCarro.id!!)
                .build()
        )
        responseObserver.onCompleted()
    }
}