package SmartRoutAPI.controller;

import SmartRoutAPI.model.Motorista;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("motorista")
public class MotoristaController {

    @PostMapping
    public void salvar(Motorista motorista){
        IO.println("Motorista recebido:" + motorista);
    }
}
