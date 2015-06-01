/* Copyright */
package ucar.nc2.ft.coverage;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ft2.coverage.CoverageDatasetFactory;
import ucar.nc2.ft2.coverage.adapter.DtCoverageCS;
import ucar.nc2.ft2.coverage.adapter.DtCoverageCSBuilder;
import ucar.nc2.ft2.coverage.adapter.DtCoverageDataset;
import ucar.nc2.ft2.coverage.grid.GridCoordSys;
import ucar.nc2.ft2.coverage.grid.GridCoverageDataset;
import ucar.unidata.test.util.NeedsCdmUnitTest;
import ucar.unidata.test.util.TestDir;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Describe
 *
 * @author caron
 * @since 6/1/2015
 */
@RunWith(Parameterized.class)
@Category(NeedsCdmUnitTest.class)
public class TestGridCoverageSubset {

    @Parameterized.Parameters(name="{0}")
    public static List<Object[]> getTestParameters() {
      List<Object[]> result = new ArrayList<>();

      result.add(new Object[]{TestDir.cdmUnitTestDir + "ft/coverage/03061219_ruc.nc", GridCoordSys.Type.Grid, 4, 4, 31});  // NUWG - has CoordinateAlias
      result.add(new Object[]{TestDir.cdmUnitTestDir + "ft/coverage/ECME_RIZ_201201101200_00600_GB", GridCoordSys.Type.Grid, 4, 5, 5});  // scalar runtime
      result.add(new Object[]{TestDir.cdmUnitTestDir + "ft/coverage/MM_cnrm_129_red.ncml", GridCoordSys.Type.Fmrc, 6, 6, 1}); // ensemble, time-offset
      result.add(new Object[]{TestDir.cdmUnitTestDir + "ft/coverage/ukmo.nc", GridCoordSys.Type.Fmrc, 4, 5, 1});              // scalar vert
      result.add(new Object[]{TestDir.cdmUnitTestDir + "ft/coverage/Run_20091025_0000.nc", GridCoordSys.Type.Curvilinear, 4, 4, 22});  // x,y axis but no projection
      result.add(new Object[]{TestDir.cdmUnitTestDir + "ft/coverage/testCFwriter.nc", GridCoordSys.Type.Grid, 3, 3, 4});  // both x,y and lat,lon
      result.add(new Object[]{TestDir.cdmUnitTestDir + "ft/fmrc/rtofs/ofs.20091122/ofs_atl.t00z.F024.grb.grib2", GridCoordSys.Type.Curvilinear, 4, 5, 7});  // GRIB Curvilinear

      return result;
    }

    String endpoint;
    GridCoordSys.Type expectType;
    int domain, range, ncoverages;


    public TestGridCoverageSubset(String endpoint, GridCoordSys.Type expectType, int domain, int range, int ncoverages) {
      this.endpoint = endpoint;
      this.expectType = expectType;
      this.domain = domain;
      this.range = range;
      this.ncoverages = ncoverages;
    }

    @Test
    public void testAdapter() throws IOException {

      try (DtCoverageDataset gds = DtCoverageDataset.open(endpoint)) {
        Assert.assertNotNull(endpoint, gds);
        Assert.assertEquals("NGrids", ncoverages, gds.getGrids().size());
        Assert.assertEquals(expectType, gds.getCoverageType());
      }

      // check DtCoverageCS
      try (NetcdfDataset ds = NetcdfDataset.openDataset(endpoint)) {
        DtCoverageCSBuilder builder = DtCoverageCSBuilder.classify(ds, null);
        Assert.assertNotNull(builder);
        DtCoverageCS cs = builder.makeCoordSys();
        Assert.assertEquals(expectType, cs.getCoverageType());
        Assert.assertEquals("NIndCoordAxes", domain, CoordinateSystem.makeDomain(cs.getCoordAxes()).size());
        Assert.assertEquals("NCoordAxes", range, cs.getCoordAxes().size());
      }
    }

    @Test
    public void testFactory() throws IOException {

      try (GridCoverageDataset gds = CoverageDatasetFactory.openGridCoverage(endpoint)) {
        Assert.assertNotNull(endpoint, gds);
        Assert.assertEquals("NGrids", ncoverages, gds.getGrids().size());
        Assert.assertEquals(expectType, gds.getCoverageType());
      }
    }
}