package com.example.clip.controller

import com.example.clip.repository.PaymentRepository
import com.example.clip.request.LoginRequest
import com.example.clip.request.PaymentRequest
import com.example.clip.request.UserRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentControllerSpec extends Specification {
    @Value('${local.server.port}')
    int port
    RestTemplate rest = new RestTemplate()

    @Autowired
    PaymentRepository paymentRepository

    def "Should create and log Payload Created Successfully"(){
        given:'a body login request'
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        LoginRequest loginRequest = new LoginRequest()
        loginRequest.with {
            user = "pinky"
            password = "pwd"
        }

        def httpEntity = new HttpEntity<Object>(loginRequest, headers)

        when:
        def response = rest.exchange("http://localhost:${ port }/login",
                HttpMethod.POST, httpEntity, UserRequest)


        then:
        assert response.statusCode == HttpStatus.OK
        assert response.body
        response.body.with {
            assert token
        }


        headers.set("Authorization", "Bearer "+response.body.token)

        String requestJson = "{}"
        HttpEntity<String> entity = new HttpEntity<>(requestJson.toString(), headers)


        PaymentRequest cmd = new PaymentRequest()
        cmd.with {
            userId = 'user'
            amount = 123
        }



        when:
        def resp = rest.exchange("http://localhost:${ port }/api/clip/createPayload", HttpMethod.POST, httpEntity, Map)


        then:
        assert  resp.statusCode == HttpStatus.OK

        when:
        def bdRow = paymentRepository.findAll()

        then:
        assert !bdRow.isEmpty()

    }



}
