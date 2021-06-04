package scripts;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.nseindia.csDashboard.dashboard.entities.*;
import com.nseindia.csDashboard.dashboard.repository.*;
import com.nseindia.csShortages.shortages.entity.*;
import com.nseindia.csShortages.shortages.repository.*;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import com.github.javafaker.Faker;
import com.nseindia.csShortages.shortages.entity.types.FullPayinStatus;
import com.nseindia.csShortages.shortages.entity.types.MemberType;
import com.nseindia.csShortages.shortages.entity.types.ObligationType;
import com.nseindia.csShortages.shortages.entity.types.ReportStatus;
import com.nseindia.csShortages.shortages.entity.types.ReportType;
import com.nseindia.csShortages.shortages.entity.types.ScheduleEvent;
import com.nseindia.csShortages.shortages.entity.types.SettlementSegment;

import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

/** insert mock data */
@SpringBootApplication
@AutoConfigurationPackage(basePackages = "com.nseindia")
public class InsertMockData implements CommandLineRunner {

  private static final Logger LOG = LoggerFactory.getLogger(InsertMockData.class);
  private static final Faker faker = new Faker();
  private static final String[] classList = { "ClassA", "ClassB", "ClassC" };

  @Autowired private ShortageRepository shortageRepository;
  @Autowired private ShortageFilterRepository shortageFilterRepository;
  @Autowired private FundsPayinRequestRepository fundsPayinRequestRepository;
  @Autowired private SettlementRepository settlementRepository;
  @Autowired private SettlementScheduleRepository settlementScheduleRepository;
  @Autowired private SettlementShortagesRepository settlementShortagesRepository;
  @Autowired private MemberMasterClearingRepository memberMasterClearingRepository;
  @Autowired private ShortageAuctionRepository shortageAuctionRepository;
  @Autowired private AuctionPaymentRepository auctionPaymentRepository;
  @Autowired private AllocatedShortageRepository allocatedShortageRepository;
  @Autowired private SystemParametersMasterRepository systemParametersMasterRepository;
  @Autowired private PhysicalSettlementFundsObligationRepository physicalSettlementFundsObligationRepository;
  @Autowired private PhysicalSettlementSecuritiesObligationRepository physicalSettlementSecuritiesObligationRepository;
  @Autowired private CashSettlementFundsObligationRepository cashSettlementFundsObligationRepository;
  @Autowired private ClientDirectPayoutRepository clientDirectPayoutRepository;
  @Autowired private MemberReportRepository memberReportRepository;

    @Autowired
    CircularsRepository circularsRepository;

    @Autowired
    SmartSuggestionRepository smartSuggestionRepository;

    @Autowired
    UserWTRepository userWTRepository;

    @Autowired
    WidgetRepository widgetRepository;

    @Autowired
    WidgetTemplateRepository widgetTemplateRepository;

	private final Random random = new Random();

  @Autowired private SettlementCalendarRepository settlementCalendarRepository;
  @Autowired private FundsRequestRepository fundsRequestRepository;
  @Autowired private FundsSummaryRepository fundsSummaryRepository;
  @Autowired private AllocateFundsEPIRepository allocateFundsEPIRepository;
  @Autowired private AllocateFundsEPIHistoryRepository allocateFundsEPIHistoryRepository;
  @Autowired private SecuritiesSummaryRepository securitiesSummaryRepository;
  @Autowired private AllocateSecuritiesEPIRepository allocateSecuritiesEPIRepository;
  @Autowired private AllocateSecuritiesEPIHistoryRepository allocateSecuritiesEPIHistoryRepository;
  @Autowired private CollateralEpiRequestRepository collateralEpiRequestRepository;

	static SettlementSegment[] segmentValues = {SettlementSegment.CASH_MARKET, SettlementSegment.FUTURES_OPTIONS, SettlementSegment.CURRENCY_DERIVATIVES,
			SettlementSegment.COMMODITIES, SettlementSegment.STOCK_LENDING_BORROWING};
	static ScheduleEvent[] scheduleEventValues = {ScheduleEvent.DPO, ScheduleEvent.PAYIN, ScheduleEvent.PAYOUT, ScheduleEvent.SHORTAGE,
			ScheduleEvent.SHORTAGE_ALLOCATION};
	
	private static String randomMemberName() {
    String name = faker.company().name();
    name = name.substring(0, Math.min(name.length(), 24));
    return name;
  }

  /**
   *
   */
  @Override
  public void run(String... args) throws Exception {
  	deleteAll();
  	SystemParametersMaster spm = new SystemParametersMaster();
  	LocalDate now = LocalDate.now();
  	spm.setCreatedDate(Date.valueOf(now.toString()));
  	spm.setCurrentSystemDate(Date.valueOf(now.toString()));
  	spm.setNextSystemDate(Date.valueOf(now.plusDays(1).toString()));
  	spm.setNextWorkingDate(Date.valueOf(now.plusDays(1).toString()));
  	spm.setPreviousSystemDate(Date.valueOf(now.minusDays(1).toString()));
  	systemParametersMasterRepository.save(spm);

  	final int membersListSize = 6;
    List<MemberMasterClearing> members = (List<MemberMasterClearing>) generateFakeData(MemberMasterClearing.class, memberMasterClearingRepository, membersListSize);
    for (int i = 0; i < members.size(); i++) {
    	members.get(i).setMemberFullName(randomMemberName());
    	members.get(i).setMemberCode(String.format("%02d", i));
    	members.get(i).setMemberType(i % 2 == 0 ? MemberType.CM : MemberType.NCL);
    }
    members = memberMasterClearingRepository.saveAll(members);
    memberMasterClearingRepository.flush();
    LOG.info("saveAll Member Master Clearing succeed, count = " + members.size());
    
    final int reportsMultiplier = 5;
    List<MemberReport> reports = new ArrayList<MemberReport>();
    for (int i = 0; i < members.size() * reportsMultiplier; i++) {
    	MemberReport report = new MemberReport();
    	report.setStatus(random.nextBoolean() ? ReportStatus.Downloaded : ReportStatus.Pending);
    	report.setReportName("Report" + String.valueOf(i));
    	report.setReportType(random.nextBoolean() ? ReportType.Position : ReportType.Results);
    	report.setSegment(SettlementSegment.values()[random.nextInt(SettlementSegment.values().length)]);
    	report.setReportId("ID0" + String.valueOf(i));
    	report.setTodayEta(random.nextInt(1439));
    	report.setModifiedEta(random.nextInt(1439));
    	report.setCmCode(members.get(i % membersListSize).getMemberCode());
    	reports.add(report);
    }
    reports = memberReportRepository.saveAll(reports);
    memberReportRepository.flush();
    LOG.info("saveAll MemberReport succeed, count = " + reports.size());
    
    List<Settlement> settlements = (List<Settlement>) generateFakeData(Settlement.class, shortageFilterRepository, 100);
    for (int i = 0; i < settlements.size(); i++) {
    	settlements.get(i).setSettleNo("N" + String.valueOf(random.nextInt(9999999)));
    	settlements.get(i).setMember(members.get(random.nextInt(members.size())));
    }
    settlements = shortageFilterRepository.saveAll(settlements);
    shortageFilterRepository.flush();
    LOG.info("saveAll Settlement succeed, count = " + settlements.size());
    
    List<ShortageAuction> shortages = (List<ShortageAuction>) generateFakeData(ShortageAuction.class, shortageRepository, 10);
    for (int i = 0; i < shortages.size(); i++) {
    	ShortageAuction s = shortages.get(i);
    	s.setSettlement(settlements.get(i % settlements.size()));
    	s.setQuantityReceivable(random.nextInt(1000));
    	s.setShortagesReceived(random.nextInt(100));
    	s.setDpoCompleted(random.nextInt(100));
    	s.setValuationPrice(random.nextDouble()*99);
    	s.setReceivedDepository(1000);
    }
    shortageRepository.saveAll(shortages);
    LOG.info("saveAll Shortage succeed, count = " + shortages.size());
    
    final int directPayoutsMultiplier = 5;
    List<ClientDirectPayout> directPayouts = (List<ClientDirectPayout>) generateFakeData(
    		ClientDirectPayout.class,
    		clientDirectPayoutRepository,
    		settlements.size() * directPayoutsMultiplier);
    for (int i = 0; i < directPayouts.size(); i++) {
    	ClientDirectPayout payout = directPayouts.get(i);
    	payout.setDpoCredit(100);
    	payout.setDpoRequest(50);
    	payout.setValuationPrice(35.0);
    	payout.setCmCode(settlements.get(i % settlements.size()).getMember().getMemberCode());
    	payout.setSettlement(settlements.get(i % settlements.size()));
    	payout.setSymbol(classList[random.nextInt(classList.length)]);
    	System.out.println(payout.toString());
    }
    directPayouts = clientDirectPayoutRepository.saveAll(directPayouts);
    clientDirectPayoutRepository.flush();
    LOG.info("saveAll ClientDirectPayout succeed, count = " + directPayouts.size());

    List<FundsPayinRequest> fundsPayinRequests = new ArrayList<FundsPayinRequest>();
    final int fundsPayinRequestMultiplier = 5;
    for (int i = 0; i < (settlements.size() * fundsPayinRequestMultiplier); i++) {
    	FundsPayinRequest fpr = new FundsPayinRequest();
    	Settlement s = settlements.get(random.nextInt(settlements.size()));
    	fpr.setCmCode(s.getMember().getMemberCode());
    	fpr.setRequestDate(Date.valueOf(now.plusDays(random.nextInt(3)).toString()));
    	fpr.setRequestTime(new Date(37800000));
    	fpr.setPayinAmount(random.nextDouble()*1000);
    	fpr.setPayinRequestStatus(random.nextBoolean() ? FullPayinStatus.COMPLETED : FullPayinStatus.INCOMPLETE);
    	fpr.setSettlement(s);
  		fundsPayinRequests.add(fpr);
    	System.out.println(fpr.toString());
    }
    fundsPayinRequests = fundsPayinRequestRepository.saveAll(fundsPayinRequests);
    fundsPayinRequestRepository.flush();
    LOG.info("saveAll FundsPayinRequest succeed, count = " + fundsPayinRequests.size());
    
    List<SettlementShortages> settlementShortages = new ArrayList<SettlementShortages>();
    final int settlementShortagesMultiplier = 5;
    for (int i = 0; i < (settlements.size() * settlementShortagesMultiplier); i++) {
      SettlementShortages ss = new SettlementShortages();
      ss.setNetObligationQuantity(random.nextInt(100000));
      ss.setFulfiledAuction(random.nextInt(100000));
      ss.setAuctionValue(random.nextDouble() * 999);
      ss.setValuationPrice(random.nextDouble() * 999);
      ss.setLastClosingPrice(random.nextDouble() * 999);
      ss.setSettlement(settlements.get(i % settlements.size()));
      if (random.nextInt(10) < 4) {
    		ss.setSegment(SettlementSegment.CASH_MARKET);
    	} else {
      	ss.setSegment(segmentValues[random.nextInt(segmentValues.length)]);    		
    	}
    	ss.setCmCode(ss.getSettlement().getMember().getMemberCode());
      ss.setSymbol(classList[random.nextInt(classList.length)]);
      settlementShortages.add(ss);
    	System.out.println(ss.toString());
    }
    settlementShortages = settlementShortagesRepository.saveAll(settlementShortages);
    settlementShortagesRepository.flush();
    LOG.info("saveAll SettlementSchedule succeed, count = " + settlementShortages.size());
    
    
    List<SettlementSchedule> settlementSchedules = new ArrayList<SettlementSchedule>();
    for (int i = 0; i < settlements.size(); i++) {
      SettlementSchedule ss = new SettlementSchedule();
    	ss.setEventCode(random.nextInt(99));
    	ss.setEventTime(random.nextInt(9999));
    	
    	ss.setSegment(segmentValues[random.nextInt(segmentValues.length)]);    		
    	ss.setEventDate(Date.valueOf(now.plusDays(random.nextInt(3)).toString()));
    	ss.setEventCode(random.nextInt(99));
    	ss.setEvent(scheduleEventValues[random.nextInt(scheduleEventValues.length)]);
    	ss.setSettlement(settlements.get(i));
    	settlementSchedules.add(ss);
    	System.out.println(ss.toString());
    }
    settlementSchedules = settlementScheduleRepository.saveAll(settlementSchedules);
    settlementScheduleRepository.flush();
    LOG.info("saveAll SettlementSchedule succeed, count = " + settlementSchedules.size());
    
    List<CashSettlementFundsObligation> cashObligations = new ArrayList<CashSettlementFundsObligation>();
    final int cashObligationMultiplier = 50;
    for (int i = 0; i < settlementSchedules.size(); i++) {
    	for (int j = 0; j < cashObligationMultiplier; j++) {
    		CashSettlementFundsObligation psfo = new CashSettlementFundsObligation();
    		psfo.setCmCode(members.get(i % membersListSize).getMemberCode());
    		psfo.setFundsPayable(random.nextDouble()*1000);
    		if (random.nextBoolean()) {
    			psfo.setFundsType(ObligationType.PAYABLE);
    		} else {
    			psfo.setFundsType(ObligationType.RECEIVABLE);
    		}
    		psfo.setSettlement(settlementSchedules.get(i).getSettlement());
    		cashObligations.add(psfo);
    	}
    }
    cashSettlementFundsObligationRepository.saveAll(cashObligations);
    LOG.info("saveAll CashSettlementFundsObligation succeed, count = " + cashObligations.size());
    
    List<PhysicalSettlementFundsObligation> physicalObligations = new ArrayList<PhysicalSettlementFundsObligation>();
    final int fundsObligationMultiplier = 15;
    for (int i = 0; i < settlementSchedules.size(); i++) {
    	for (int j = 0; j < fundsObligationMultiplier; j++) {
    		PhysicalSettlementFundsObligation psfo = new PhysicalSettlementFundsObligation();
    		psfo.setEpiAmount(random.nextDouble()*1000);
    		psfo.setEpiAmountCollateral(random.nextDouble()*100);
    		psfo.setFullPayinStatus(random.nextBoolean() ? FullPayinStatus.COMPLETED : FullPayinStatus.INCOMPLETE);
    		psfo.setObligationValue(random.nextDouble()*1000);
    		psfo.setSettlement(settlementSchedules.get(i).getSettlement());
    		psfo.setSegment(settlementSchedules.get(i).getSegment());
    		physicalObligations.add(psfo);
    	}
    }
    physicalSettlementFundsObligationRepository.saveAll(physicalObligations);
    LOG.info("saveAll PhysicalSettlementFundsObligation succeed, count = " + physicalObligations.size());
    
    List<PhysicalSettlementSecuritiesObligation> settlementSecuritiesObligation = new ArrayList<PhysicalSettlementSecuritiesObligation>();
    final int securitiesObligationMultiplier = 15;
    for (int i = 0; i < settlementSchedules.size(); i++) {
    	for (int j = 0; j < securitiesObligationMultiplier; j++) {
    		PhysicalSettlementSecuritiesObligation psso = new PhysicalSettlementSecuritiesObligation();
    		psso.setClientCode("CM-" + String.valueOf(i) + String.valueOf(j));
    		psso.setNetObligationQuantity(random.nextInt(1000));
    		psso.setObligationValue(random.nextDouble()*1000);
    		psso.setSettlement(settlementSchedules.get(i).getSettlement());
    		psso.setSegment(settlementSchedules.get(i).getSegment());
    		settlementSecuritiesObligation.add(psso);
    	}
    }
    physicalSettlementSecuritiesObligationRepository.saveAll(settlementSecuritiesObligation);
    LOG.info("saveAll PhysicalSettlementSecuritiesObligation succeed, count = " + settlementSecuritiesObligation.size());

    List<ShortageAuction> shortageAuctions = (List<ShortageAuction>) generateFakeData(ShortageAuction.class, shortageAuctionRepository, 10);
    for (int i=0;i<shortageAuctions.size();i++){
        shortageAuctions.get(i).setSettlement(settlements.get(i % settlements.size()));
    }
    shortageAuctionRepository.saveAll(shortageAuctions);
    LOG.info("saveAll ShortageAuction succeed, count = " + shortageAuctions.size());
    
    List<AuctionPayment> auctionPayments = (List<AuctionPayment>) generateFakeData(AuctionPayment.class, auctionPaymentRepository, 10);
    auctionPaymentRepository.saveAll(auctionPayments);
    LOG.info("saveAll AuctionPayment succeed, count = " + auctionPayments.size());
    
    List<AllocatedShortage> allocatedShortages = (List<AllocatedShortage>) generateFakeData(AllocatedShortage.class, allocatedShortageRepository, 10);
    for (int i = 0; i < allocatedShortages.size(); i++) {
    	AllocatedShortage as = allocatedShortages.get(i);
    	as.setSettlement(settlements.get(i % settlements.size()));
    	as.setDpoCompleted(random.nextInt(100));
    	as.setQuantityReceivable(random.nextInt(1000));
    	as.setShortagesReceived(random.nextInt(100));
    	as.setValuationPrice(random.nextDouble()*99);
    }
    allocatedShortageRepository.saveAll(allocatedShortages);
    LOG.info("saveAll AllocatedShortage succeed, count = " + allocatedShortages.size());

      List<Widget> widgets = createWidgets();
      createTemplate(widgets);
      createCirculars();
      createSmartSuggestion();
    

    SettlementCalendar calendar1 = new SettlementCalendar();
    calendar1.setStpType("N");
    calendar1.setStpNo("2020184");
    calendar1.setSettlementDate(Date.valueOf(now.plusDays(random.nextInt(3)).toString()));
    calendar1.setPayinTime(Time.valueOf("22:00:00"));
    settlementCalendarRepository.save(calendar1);

    SettlementCalendar calendar2 = new SettlementCalendar();
    calendar2.setStpType("N");
    calendar2.setStpNo("2020185");
    calendar2.setSettlementDate(Date.valueOf(now.plusDays(random.nextInt(3)).toString()));
    calendar2.setPayinTime(Time.valueOf("23:00:00"));
    settlementCalendarRepository.save(calendar2);

    for (var segment : SettlementSegment.values()) {
      for (var symbol : Arrays.asList("ASIANPAINT", "EUROPAINT")) {
        for (var series : Arrays.asList("EQ", "PT")) {
          for (var stpNo : Arrays.asList("2020184", "2020185")) {
            var summary = new SecuritiesSummary();
            summary.setClearingMemCd("00");
            summary.setSegment(segment);
            summary.setSymbol(symbol);
            summary.setSeries(series);
            summary.setStpType("N");
            summary.setStpNo(stpNo);
            summary.setIsin(symbol + series);
            summary.setReceivedFromDepository(new BigDecimal("1000000"));
            summary.setUtilizedQuantity(new BigDecimal(0));
            summary.setUnutilizedAllocation(new BigDecimal(0));
            securitiesSummaryRepository.save(summary);
          }
        }
      }
    }

    // COLLATERAL_EPI_REQUEST
    List<CollateralEpiRequest> collateralEpiRequests = new ArrayList<>();
    final Double MIN_ALLOCATE = 100.0;
    final Double ALLOCATE_RANGE = 10000.0;
    Long rn = 1L;
    for (Settlement s : settlements) {
      CollateralEpiRequest cer = new CollateralEpiRequest();
      cer.setAllocateCash(MIN_ALLOCATE + random.nextDouble() * ALLOCATE_RANGE);
      LocalDate date = now.plusDays(random.nextInt(5));
      cer.setSettlementDate(java.time.LocalDate.of(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth()));
      cer.setRequestNumber(rn);
      collateralEpiRequests.add(cer);
      rn++;
    }
    collateralEpiRequestRepository.saveAll(collateralEpiRequests);
  }

  /**
   * generate mock data
   *
   * @param tClass the entity class
   * @param repository the entity repository
   * @param <T> the entity type
   * @param <K> the entity ID type
   */
  private <T, K> List<T> generateFakeData(Class<T> tClass, JpaRepository<T, K> repository, int count)
      throws Exception {

  	LOG.info("try to generate mocks for " + tClass.getName());
    repository.deleteAllInBatch();
    LOG.info("deleteAllInBatch done");
    List<T> rows = new ArrayList<>();
    PodamFactory factory = new PodamFactoryImpl();

    RandomObjectFiller filler = new RandomObjectFiller();
    System.out.println("factory created");

    for (int i = 0; i < count; i++) {
      T pojo = factory.manufacturePojo(tClass);

      filler.fill(pojo);
      rows.add(pojo);
      
      System.out.println(pojo.toString());
    }
    return rows;
  }

    /**
     * create widgets
     *
     * @return the widgets
     */
    private List<Widget> createWidgets() {
        List<Widget> rows =
                Arrays.asList(
                        new Widget(
                                "trade_enquiry",
                                "Trade enquiry dashboard (client level)",
                                "All",
                                "overall-trade-overview.component",
                                "1,1",
                                "1,1",
                                true),
                        new Widget("Dpo", "DPO dashboard", "Trading Member", "CSO-685", "2,2", "1,2", true),
                        new Widget(
                                "client_level_security",
                                "Client level security table/dashboard",
                                "All",
                                "CM-100",
                                "1,1",
                                "1,1",
                                true),
                        new Widget(
                                "excess_collateral", "Excess collateral", "All", "CM-125", "1,1", "1,1", true),
                        new Widget(
                                "smart-suggestions",
                                "smart suggestions",
                                "All",
                                "smart-suggestions",
                                "1,1",
                                "1,1",
                                true),
                        new Widget("circulars", "circulars", "All", "circulars", "2,1", "2,1", true),
                        new Widget("settlement_cal", "Settlement Calendar", "All", "settlement_cal", "1,2", "1,2", true));
        for (Widget row : rows) {
            widgetRepository.save(row);
        }
        return rows;
    }

    /** create smart suggestions */
    private void createSmartSuggestion() {
        for (int i = 0; i < 30; i++) {
            SmartSuggestion smartSuggestion = new SmartSuggestion();
            smartSuggestion.setActive(true);
            smartSuggestion.setCondition(null);
            smartSuggestion.setDeadlineTime(new java.util.Date());
            smartSuggestion.setMemberType("Member admin");
            smartSuggestion.setDurationBeforeDeadline(0);
            smartSuggestion.setFrequency(FrequencyType.Daily);
            smartSuggestion.setHeader("Home Dashboard " + i);
            smartSuggestion.setMessage("Set up Home Dashboard " + i);
            smartSuggestion.setLink(
                    "http://15.207.122.114/nse-clearing-settlement/#/main/trade/dashboard/" + i);

            smartSuggestionRepository.save(smartSuggestion);
        }
    }

    /** create circulars */
    private void createCirculars() {
        for (int i = 0; i < 30; i++) {
            Circulars circulars = new Circulars();
            circulars.setActive(true);
            circulars.setCircularIdentifier("NSCCL/CMPT/45818/" + i);
            circulars.setDate(new java.util.Date());
            circulars.setDownloadLink("http://test.com/qwssdeedd/" + i);
            circulars.setJourneyName("Clearing");
            circulars.setTitle("Test-" + i);
            circulars.setTab(i % 2 == 0 ? "Tab1" : "Tab2");
            circulars.setSourceName(i % 2 == 0 ? "Source1" : "Source2");
            circularsRepository.save(circulars);
        }
    }

    private void createTemplate(List<Widget> widgets) {
        int i = 0;
        for (Widget widget : widgets) {
            WidgetTemplate widgetTemplate = new WidgetTemplate();
            widgetTemplate.setActive(true);
            widgetTemplate.setMemberRole("All");
            widgetTemplate.setIsDefault(true);
            widgetTemplate.setWidget(widget);
            widgetTemplate.setCalculatedComponentSize(widget.getComponentSize());
            widgetTemplate.setPositionRowColumn((i/4)+","+i%4);
            widgetTemplate.setWorkspaceId("1");
            widgetTemplate.setWorkspaceName("WorkspaceName for ALL");
            widgetTemplateRepository.save(widgetTemplate);
            i++;
        }
    }

  private void deleteAll() {
  	memberReportRepository.deleteAllInBatch();
    systemParametersMasterRepository.deleteAllInBatch();
    allocatedShortageRepository.deleteAllInBatch();
    auctionPaymentRepository.deleteAllInBatch();
    shortageAuctionRepository.deleteAllInBatch();
    cashSettlementFundsObligationRepository.deleteAllInBatch();
    physicalSettlementFundsObligationRepository.deleteAllInBatch();
    physicalSettlementSecuritiesObligationRepository.deleteAllInBatch();
    settlementScheduleRepository.deleteAllInBatch();
    clientDirectPayoutRepository.deleteAllInBatch();
    shortageRepository.deleteAllInBatch();
    fundsPayinRequestRepository.deleteAllInBatch();
    settlementShortagesRepository.deleteAllInBatch();
    settlementRepository.deleteAllInBatch();
    shortageFilterRepository.deleteAllInBatch();
    memberMasterClearingRepository.deleteAllInBatch();
      circularsRepository.deleteAllInBatch();
      smartSuggestionRepository.deleteAllInBatch();
      userWTRepository.deleteAllInBatch();
      widgetTemplateRepository.deleteAllInBatch();
      widgetRepository.deleteAllInBatch();
      smartSuggestionRepository.deleteAllInBatch();
    settlementCalendarRepository.deleteAllInBatch();
    fundsRequestRepository.deleteAllInBatch();
    fundsSummaryRepository.deleteAllInBatch();
    allocateFundsEPIRepository.deleteAllInBatch();
    allocateFundsEPIHistoryRepository.deleteAllInBatch();
    securitiesSummaryRepository.deleteAllInBatch();
    allocateSecuritiesEPIRepository.deleteAllInBatch();
    allocateSecuritiesEPIHistoryRepository.deleteAllInBatch();
  }
  /**
   * entry
   *
   * @param args the cli args
   */
  public static void main(String[] args) {
    LOG.info("STARTING THE MOCK APPLICATION");

    SpringApplication application = new SpringApplication(InsertMockData.class);
    application.setWebApplicationType(WebApplicationType.NONE);
    application.run(args);

    LOG.info("APPLICATION FINISHED");
    System.exit(0);
  }
}
