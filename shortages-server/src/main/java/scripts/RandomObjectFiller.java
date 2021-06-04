package scripts;

import java.beans.PropertyDescriptor;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Random;

import org.springframework.beans.BeanUtils;

import com.nseindia.csShortages.shortages.entity.AllocatedShortageId;
import com.nseindia.csShortages.shortages.entity.AuctionPaymentId;
import com.nseindia.csShortages.shortages.entity.Settlement;
import com.nseindia.csShortages.shortages.entity.ShortageAuction;

/** Random object filler. */
public class RandomObjectFiller {
  private final Random random = new Random();

  private int month = 0;
  private int memberCount = 0;
  private int stpNoCount = 0;

  /**
   * Fill object with random value.
   *
   * @param instance The object instance
   */
  public void fill(Object instance) throws Exception {

    for (PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(instance.getClass())) {
      if (pd.getWriteMethod() != null && pd.getReadMethod() != null) {
        if (pd.getName().equals("id") && pd.getPropertyType() == Integer.class) {
          pd.getWriteMethod().invoke(instance, new Object[] {null});
          continue;
        }
        Object value = getRandomValueForField(instance, pd);
        if (value != null) {
          pd.getWriteMethod().invoke(instance, value);
        }
      }
    }

    if (instance instanceof ShortageAuction) {
      int mod = random.nextInt(2) + 1;
      ((ShortageAuction) instance).setClientCode("tm-" + mod);
      ((ShortageAuction) instance).setClientName("tm-" + mod + " name");
      ((ShortageAuction) instance).setSecurityType("st-" + mod);
      mod = random.nextInt(2) + 1;
      ((ShortageAuction) instance).setClearingMemberCD("sp-" + mod);
      ((ShortageAuction) instance).setValuationPrice(random.nextDouble()*999);
    }

    if (instance instanceof Settlement) {
      int mod = random.nextInt(2) + 1;
      ((Settlement) instance).setClientName("tm-" + mod + " name");
      ((Settlement) instance).setSecurityType("st-" + mod);
      ((Settlement) instance).setAggregatedFlag("af-" + mod);
    }
  }

  /**
   * Get random value.
   *
   * @param instance the object instance
   * @param pd the PropertyDescriptor
   * @return random value
   */
  private Object getRandomValueForField(Object instance, PropertyDescriptor pd) throws Exception {
    Class<?> type = pd.getPropertyType();
    if (type.equals(Double.TYPE) || type.equals(Double.class)) {
      return Math.round(random.nextDouble() * 1000000.0) / 100.0;
    }
    if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
      return random.nextInt(999999999);
    }
    if (pd.getName().equals("settlementDate")) {
      return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }
    if (pd.getName().equals("clearingMemId") || pd.getName().equals("memberId")) {
      return "id-" + ((memberCount++ % 5) + 1);
    }
    if (pd.getName().equals("stpNo") || pd.getName().equals("settleNo")) {
      return "no-" + ((stpNoCount++ % 3) + 1);
    }
    if (pd.getName().equals("stpStartDate")) {
      return java.sql.Date.valueOf(LocalDate.of(2020, 1, 1));
    }
    if (pd.getName().equals("stpEndDate")) {
      LocalDate localDate = LocalDate.of(2020, (month % 12) + 1, 2);
      month++;
      return java.sql.Date.valueOf(localDate);
    }

    if (type.equals(AuctionPaymentId.class) || type.equals(AllocatedShortageId.class)) {
      fill(pd.getReadMethod().invoke(instance));
      return null;
    }
    return null;
  }
}
