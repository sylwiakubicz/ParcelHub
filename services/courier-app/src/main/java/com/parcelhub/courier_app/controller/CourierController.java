package com.parcelhub.courier_app.controller;

import com.parcelhub.courier_app.dto.ArrivedAtHubRequestDto;
import com.parcelhub.courier_app.dto.CollectRequestDto;
import com.parcelhub.courier_app.dto.ResponseDto;
import com.parcelhub.courier_app.service.CourierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courier")
public class CourierController {

    private final CourierService courierService;

    public CourierController(CourierService courierService) {
        this.courierService = courierService;
    }

    @PostMapping("{courierId}/collect")
    public ResponseEntity<ResponseDto> collect(@PathVariable String courierId,
                                               @RequestBody CollectRequestDto collectRequestDto) {
        courierService.collect(courierId, collectRequestDto.getShipmentId());
        return ResponseEntity.ok().body(
                new ResponseDto(courierId, collectRequestDto.getShipmentId(),"collected from locker"));
    }

    @PostMapping("{courierId}/hub-arrival")
    public ResponseEntity<ResponseDto> hubArrival(@PathVariable String courierId,
                                                  @RequestBody ArrivedAtHubRequestDto arrivedAtHubRequestDto) {
        courierService.arrivedAtHub(courierId, arrivedAtHubRequestDto.getHubId(), arrivedAtHubRequestDto.getShipmentId());
        return ResponseEntity.ok().body(
                new ResponseDto(courierId, arrivedAtHubRequestDto.getShipmentId() ,"arrived at hub"));
    }

}
