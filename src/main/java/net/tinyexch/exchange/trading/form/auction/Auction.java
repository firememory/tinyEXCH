package net.tinyexch.exchange.trading.form.auction;

import net.tinyexch.exchange.trading.form.StateChangeListener;
import net.tinyexch.exchange.trading.form.TradingForm;
import net.tinyexch.order.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singleton;
import static net.tinyexch.exchange.trading.form.auction.AuctionState.*;

/**
 * This mode resembles reflects an auction technical wise. A provided OB will be balanced after the best price
 * could be determined.
 *
 * @author ratzlow@gmail.com
 * @since 2014-08-02
 */
public class Auction extends TradingForm<AuctionState> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Auction.class);

    private final CallPhase callPhase;
    private final PriceDeterminationPhase priceDeterminationPhase;
    private final OrderbookBalancingPhase orderbookBalancingPhase;

    //-------------------------------------------------------------------------------------
    // constructors
    //-------------------------------------------------------------------------------------

    public Auction( StateChangeListener<AuctionState> stateChangeListener,
                    CallPhase callPhase,
                    PriceDeterminationPhase priceDeterminationPhase,
                    OrderbookBalancingPhase orderbookBalancingPhase ) {

        super(stateChangeListener);
        this.callPhase = callPhase;
        this.priceDeterminationPhase = priceDeterminationPhase;
        this.orderbookBalancingPhase = orderbookBalancingPhase;
    }

    public Auction() {
        super();
        callPhase = order -> LOGGER.info("Accepted order: {}", order);
        priceDeterminationPhase = () -> {};
        orderbookBalancingPhase = () -> {};
    }

    //-------------------------------------------------------------------------------------
    // different auction phases
    //-------------------------------------------------------------------------------------

    public void startCallPhase() {
        transitionTo( CALL_RUNNING );
    }

    // TODO (FRa) : (FRa) : ensure validations are called upfront; maybe add check to auction phase upfront as well
    public void place( Order order ) {
        if ( getCurrentState() != CALL_RUNNING ) {
            String messagemsg = "Call phase not opened so cannot accept order! Current state is = " +
                    getCurrentState();
            throw new AuctionException(messagemsg);
        }

        callPhase.accept( order );
    }

    public void stopCallPhase() {
        transitionTo( INACTIVE );
    }

    public void determinePrice() {
        transitionTo( PRICE_DETERMINATION_RUNNING );
        priceDeterminationPhase.determinePrice();
        transitionTo(INACTIVE);
    }

    public void balanceOrderbook() {
        transitionTo( ORDERBOOK_BALANCING_RUNNING );
        orderbookBalancingPhase.balance();
        transitionTo(INACTIVE);
    }


    @Override
    protected Map<AuctionState, Set<AuctionState>> getAllowedTransitions() {
        Map<AuctionState, Set<AuctionState>> transitions = new EnumMap<>(AuctionState.class);
        transitions.put(INACTIVE, EnumSet.of(CALL_RUNNING, PRICE_DETERMINATION_RUNNING, ORDERBOOK_BALANCING_RUNNING) );
        transitions.put(CALL_RUNNING, singleton(INACTIVE));
        transitions.put(PRICE_DETERMINATION_RUNNING, singleton(INACTIVE) );
        transitions.put(ORDERBOOK_BALANCING_RUNNING, singleton(INACTIVE) );

        return transitions;
    }

    @Override
    public AuctionState getDefaultState() { return INACTIVE; }
}