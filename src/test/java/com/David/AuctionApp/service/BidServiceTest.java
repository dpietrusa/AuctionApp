package com.David.AuctionApp.service;

import com.David.AuctionApp.exception.InvalidBidAmountException;
import com.David.AuctionApp.exception.OutbidException;
import com.David.AuctionApp.exception.ReserveNotMetException;
import com.David.AuctionApp.model.Auctionitem;
import com.David.AuctionApp.model.request.SubmitBidRequest;
import com.David.AuctionApp.repository.AuctionitemRepository;
import com.David.AuctionApp.repository.BidRepository;
import com.David.AuctionApp.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;

import static com.David.AuctionApp.util.BuildObjects.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class BidServiceTest {
    @InjectMocks
    @Spy
    BidService bidService;
    @Mock
    AuctionitemRepository auctionitemRepository;
    @Mock
    BidRepository bidRepository;
    @Mock
    UserRepository userRepository;

    @Test(expected = ReserveNotMetException.class)
    public void submitBidShouldThrowExceptionIfReserveIsNotMet() throws ReserveNotMetException, OutbidException, InvalidBidAmountException {
        SubmitBidRequest submitBidRequest = buildSubmitBidRequest();
        submitBidRequest.setMaxAutoBidAmount(new BigDecimal(500));
        Mockito.when(auctionitemRepository.getOne(Mockito.anyLong())).thenReturn(buildAuctionItem());
        Mockito.when(bidRepository.save(Mockito.any())).thenReturn(buildBid());
        bidService.submitBid(submitBidRequest);
    }

    @Test(expected = InvalidBidAmountException.class)
    public void submitBidShouldThrowExceptionIfAutoBidAmountIsLessThanCurrentBid() throws ReserveNotMetException, OutbidException, InvalidBidAmountException {
        Auctionitem auctionItem = buildAuctionItem();
        auctionItem.setCurrentBid(new BigDecimal(80000.00));
        Mockito.when(auctionitemRepository.getOne(Mockito.anyLong())).thenReturn(auctionItem);
        bidService.submitBid(buildSubmitBidRequest());
    }

    @Test(expected = OutbidException.class)
    public void submitBidShouldThrowExceptionWhenUserHasBeenOutbid() throws ReserveNotMetException, OutbidException, InvalidBidAmountException {
        Auctionitem auctionItem = buildAuctionItem();
        auctionItem.setReservePriceMet(true);
        Mockito.when(auctionitemRepository.getOne(Mockito.anyLong())).thenReturn(auctionItem);
        Mockito.when(bidRepository.findMaxAutoBidForAuctionItem(Mockito.anyLong())).thenReturn(new BigDecimal(80000));
        Mockito.when(bidRepository.save(Mockito.any())).thenReturn(buildBid());
        bidService.submitBid(buildSubmitBidRequest());
    }

    @Test
    public void submitBidShouldReturnMessageNotifyingHighestBidderAndCurrentBid() throws ReserveNotMetException, OutbidException, InvalidBidAmountException {
        Auctionitem auctionItem = buildAuctionItem();
        auctionItem.setReservePriceMet(true);
        Mockito.when(auctionitemRepository.getOne(Mockito.anyLong())).thenReturn(auctionItem);
        Mockito.when(bidRepository.findMaxAutoBidForAuctionItem(Mockito.anyLong())).thenReturn(new BigDecimal(2000));
        Mockito.when(bidRepository.save(Mockito.any())).thenReturn(buildBid());
        String actual = bidService.submitBid(buildSubmitBidRequest());
        assertEquals("You are the highest bidder. Current bid: $2001", actual);
    }
}
