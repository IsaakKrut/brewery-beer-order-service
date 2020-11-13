package guru.sfg.beer.order.service.services.listeners;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.events.AllocateOrderResult;
import guru.sfg.brewery.model.events.ValidateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class AllocationResultListener {
    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE)
    public void listen(AllocateOrderResult result){
        BeerOrderDto beerOrder = result.getBeerOrderDto();

        log.debug("Allocation Result for Order Id: " + beerOrder.getId());

        if (result.getAllocationError()){
            //allocation error
            beerOrderManager.beerOrderAllocationFailed(beerOrder);
        }
        else if(result.getPendingInventory()){
            //pending inventory
            beerOrderManager.beerOrderAllocationPendingInventory(beerOrder);
        } else{
            //allocated normally
            beerOrderManager.beerOrderAllocationPassed(beerOrder);
        }

    }
}
