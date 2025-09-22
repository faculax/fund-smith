package com.vibe.fundsmith.service;

import com.vibe.fundsmith.model.Trade;
import com.vibe.fundsmith.repository.TradeRepository;
import com.vibe.fundsmith.dto.TradeRequest;
import com.vibe.fundsmith.exception.ValidationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class TradeService {
    private static final Logger logger = LoggerFactory.getLogger(TradeService.class);
    private static final Pattern ISIN_PATTERN = Pattern.compile("^[A-Z0-9]{12}$");
    private final TradeRepository tradeRepository;

    public TradeService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }
    
    /**
     * Deletes all trades from the database
     * @return The number of trades deleted
     */
    @Transactional
    public long deleteAllTrades() {
        long count = tradeRepository.count();
        logger.info("Deleting all {} trades from the database", count);
        tradeRepository.deleteAll();
        return count;
    }

    @Transactional
    public Trade bookTrade(TradeRequest request) {
        validateTradeRequest(request);
        
        LocalDate settleDate = request.getSettleDate() != null 
            ? request.getSettleDate() 
            : calculateSettlementDate(request.getTradeDate());
            
        Trade trade = new Trade(
            request.getIsin(),
            request.getQuantity(),
            request.getPrice(),
            request.getTradeDate(),
            settleDate
        );
        
        return tradeRepository.save(trade);
    }

    public List<Trade> findTrades(LocalDate fromDate, LocalDate toDate, String isin, Integer limit) {
        int actualLimit = limit != null ? limit : 50;
        PageRequest pageRequest = PageRequest.of(0, actualLimit);
        
        if (fromDate != null && toDate != null) {
            return isin != null 
                ? tradeRepository.findByIsinAndTradeDateBetweenOrderByCreatedAtDescIdDesc(isin, fromDate, toDate, pageRequest)
                : tradeRepository.findByTradeDateBetweenOrderByCreatedAtDescIdDesc(fromDate, toDate, pageRequest);
        }
        
        return isin != null
            ? tradeRepository.findByIsinOrderByCreatedAtDescIdDesc(isin, pageRequest)
            : tradeRepository.findByOrderByCreatedAtDescIdDesc(pageRequest);
    }

    private void validateTradeRequest(TradeRequest request) {
        if (!ISIN_PATTERN.matcher(request.getIsin()).matches()) {
            throw new ValidationException("isin", "Invalid ISIN format");
        }
        
        if (request.getQuantity() <= 0) {
            throw new ValidationException("quantity", "Quantity must be positive");
        }
        
        if (request.getPrice().signum() <= 0) {
            throw new ValidationException("price", "Price must be positive");
        }
        
        if (request.getTradeDate().isAfter(LocalDate.now())) {
            throw new ValidationException("tradeDate", "Trade date cannot be in the future");
        }
        
        if (request.getSettleDate() != null && request.getSettleDate().isBefore(request.getTradeDate())) {
            throw new ValidationException("settleDate", "Settlement date cannot be before trade date");
        }
    }

    private LocalDate calculateSettlementDate(LocalDate tradeDate) {
        LocalDate settleDate = tradeDate;
        int daysToAdd = 2;
        
        while (daysToAdd > 0) {
            settleDate = settleDate.plusDays(1);
            if (!isWeekend(settleDate)) {
                daysToAdd--;
            }
        }
        
        return settleDate;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}