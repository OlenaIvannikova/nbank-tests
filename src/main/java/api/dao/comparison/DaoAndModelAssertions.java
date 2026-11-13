package api.dao.comparison;

import api.models.BaseModel;
import org.assertj.core.api.AbstractAssert;

public class DaoAndModelAssertions {

  private static final DaoComparator daoComparator = new DaoComparator();

  public static DaoModelAssert assertThat(BaseModel baseModel, Object daoModel) {
    return new DaoModelAssert(baseModel, daoModel);
  }

  public static class DaoModelAssert extends AbstractAssert<DaoModelAssert, Object> {
    private final BaseModel baseModel;
    private final Object daoModel;

    public DaoModelAssert(BaseModel baseModel, Object daoModel) {
      super(baseModel, DaoModelAssert.class);
      this.baseModel = baseModel;
      this.daoModel = daoModel;
    }

    public DaoModelAssert match() {
      if (baseModel == null) {
        failWithMessage("API model should not be null");
      }

      if (daoModel == null) {
        failWithMessage("DAO model should not be null");
      }

      // Use configurable comparison
      try {
        daoComparator.compare(baseModel, daoModel);
      } catch (AssertionError e) {
        failWithMessage(e.getMessage());
      }

      return this;
    }
  }
}
