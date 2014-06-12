package org.duffqiu.rest.test.dsl

import scala.language.implicitConversions
import scala.language.postfixOps

import org.duffqiu.rest.common.RestOperation
import org.duffqiu.rest.common.RestRequest
import org.duffqiu.rest.common.RestResource
import org.duffqiu.rest.common.RestResponse
import org.duffqiu.rest.common.RestServer

object RestServerTestDsl {
    type Server = RestServer
    type WithServerResource = (Server, RestResource)
    type WithServerResourceOperation = (Server, RestResource, RestOperation)
    type WithServerResourceOperationRequest[A] = (Server, RestResource, RestOperation, A)
    type Request2Response[A, B] = A => B

    class ServerHelper(serv: Server) {
        def own(resource: RestResource) = (serv, resource)
        def and(resource: RestResource) = (serv, resource)
        def run = serv.startup
        def end = serv
    }

    class ResourceHelper(wsr: WithServerResource) {
        def when(operation: RestOperation) = (wsr._1, wsr._2, operation)
    }

    class OperationHelper(wsro: WithServerResourceOperation) {
        def given[A](request: A) = (wsro._1, wsro._2, wsro._3, request)
    }

    class RequestHelper[A](wsror: WithServerResourceOperationRequest[A]) {
        def then[B](fun: Request2Response[A, B]) = (wsror._1, wsror._2, wsror._3, wsror._4, fun)
    }

    implicit def string2RestServerHelper(name: String) = new RestServer(name)
    implicit def server2ServerHelper(serv: Server) = new ServerHelper(serv)
    implicit def withServerResource(wsr: WithServerResource) = new ResourceHelper(wsr)
    implicit def withServerOperation(wsro: WithServerResourceOperation) = new OperationHelper(wsro)
    implicit def withServerRequest[A <: RestRequest](wsror: WithServerResourceOperationRequest[A]) = new RequestHelper(wsror)

    implicit def Tuple2Server[A <: RestRequest, B <: RestResponse](t: (Server, RestResource, RestOperation, A, Request2Response[A, B])): ServerHelper = {
        t match {
            case ((server, resource, operation, request, req2resp)) => {

                val response = req2resp(request)

                server.configMock(resource, operation, request, response)

                //println("\t*" + server.name + " own " + resource.path + "(" + resource.style + ")" + " when " + operation() + " given " + request + " then " + response)

            }

        }

        new ServerHelper(t._1)
    }

}