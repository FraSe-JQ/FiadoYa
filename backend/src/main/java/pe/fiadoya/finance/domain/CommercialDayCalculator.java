package pe.fiadoya.finance.domain;

import java.time.LocalDate;

public final class CommercialDayCalculator {
  /**
   * Calcula data=360*(Y2-Y1)+30*(M2-M1)+(D2-D1), base 30E/360 con finales de mes normalizados.
   * Entrada: fechas calendario sin hora; cada fin de mes, incluido febrero, se transforma en
   * dataíaccess 30. Salida: dataías comerciales enteros con signo; no existe redondeo monetario ni
   * tasa. Fechas iguales producen cero. Diferir del calendario es intencional. Informe 4.2.
   */
  public int between(LocalDate start, LocalDate end) {
    int startDay = normalizedDay(start), endDay = normalizedDay(end);
    return 360 * (end.getYear() - start.getYear())
        + 30 * (end.getMonthValue() - start.getMonthValue())
        + endDay
        - startDay;
  }

  private int normalizedDay(LocalDate date) {
    return date.getDayOfMonth() == date.lengthOfMonth() ? 30 : Math.min(date.getDayOfMonth(), 30);
  }
}
