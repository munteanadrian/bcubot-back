package com.bcubot.bcubot.controller;

import com.bcubot.bcubot.Service.BotService;
import com.bcubot.bcubot.model.Client;
import com.bcubot.bcubot.model.Result;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
@RequestMapping("/")
public class BotController {
    private final BotService botService;

    public BotController(BotService botService) {
        this.botService = botService;
    }

    @PostMapping("/book")
    public Result book(@RequestBody Client client) throws Exception {
        return botService.startBooking(client);
    }

    @PostMapping("/delete")
    public Result delete(@RequestBody Client client) throws Exception {
        return botService.deleteBooking(client);
    }

    @GetMapping("/bookedSeat")
    public String getBookedSeat() {
        return botService.getBookedSeat();
    }
}
