package net.tinyexch.ob.match;

import net.tinyexch.order.Order;
import net.tinyexch.order.OrderType;
import net.tinyexch.order.Side;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Check combinations of orders and their sorting. Market orders be on top if by price check.
 *
 * @author ratzlow@gmail.com
 * @since 2014-12-16
 */
public class PrioritiesTest {

    @Test
    public void testBuyFirstMarketThanHighestPrice() {
        List<Order> buys = createUnsortedOrders(Side.BUY);
        // sort them
        buys.sort(Priorities.PRICE.reversed());
        List<String> sortedBuyIDs = buys.stream().map(Order::getClientOrderID).collect(toList());
        Assert.assertEquals("Buy side: MKT first, than highest limit, than lower limit",
                Arrays.asList("3", "2", "1"), sortedBuyIDs);
    }

    @Test
    public void testSellFirstMarketThanLowestPrice() {
        List<Order> sells = createUnsortedOrders(Side.SELL);
        sells.sort(Priorities.PRICE);
        List<String> sortedSellIDs = sells.stream().map(Order::getClientOrderID).collect(toList());
        Assert.assertEquals( "Sell side: MKT first, than lowest limit, than higher limit",
                Arrays.asList("3", "1", "2"), sortedSellIDs );
    }

    private List<Order> createUnsortedOrders(Side side) {
        Order o1 = Order.of("1", side).setPrice(87).setOrderType(OrderType.LIMIT).setOrderQty(18);
        Order o2 = Order.of("2", side).setPrice(89).setOrderType(OrderType.LIMIT).setOrderQty(20);
        Order o3 = Order.of("3", side).setOrderType(OrderType.MARKET).setOrderQty(70);
        return Arrays.asList(o1, o2, o3);
    }
}
