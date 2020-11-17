package guru.sfg.beer.order.service.services.testcomponents;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.brewery.model.events.AllocateOrderRequest;
import guru.sfg.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAllocationListener {

    private final JmsTemplate jmsTemplate;


    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(Message message){

        AllocateOrderRequest request = (AllocateOrderRequest) message.getPayload();
        boolean pendingInventory = false;
        boolean allocationError = false;
        boolean sendResponse = true;


        // set allocation error
        if (request.getBeerOrderDto().getCustomerRef() != null) {
            if (request.getBeerOrderDto().getCustomerRef().equals("failed-allocation")) {
                allocationError = true;
            } else if (request.getBeerOrderDto().getCustomerRef().equals("partial-allocation")) {
                pendingInventory = true;
            } else if (request.getBeerOrderDto().getCustomerRef().equals("dont-allocate")) {
                sendResponse = false;
            }
        }


        boolean finalPendingInventory = pendingInventory;
        request.getBeerOrderDto().getBeerOrderLines().forEach(beerOrderLineDto -> {
            if (finalPendingInventory){
                beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity() - 1);

            }else {
                beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity());
            }
            });
        if (sendResponse) {
            jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
                    AllocateOrderResult.builder()
                            .allocationError(allocationError)
                            .pendingInventory(pendingInventory)
                            .beerOrderDto(request.getBeerOrderDto())
                            .build());
        }
    }
}
