package com.riiablo.map2;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import com.riiablo.Riiablo;
import com.riiablo.RiiabloTest;
import com.riiablo.io.ByteInput;
import com.riiablo.logger.Level;
import com.riiablo.logger.LogManager;
import com.riiablo.map2.DT1.Tile;
import com.riiablo.map2.DT1.Tile.Block;

public class DT1Test extends RiiabloTest {
  @BeforeAll
  public static void before() {
    LogManager.setLevel("com.riiablo.map2", Level.TRACE);
  }

  private DT1 testDt1(FileHandle handle) {
    ByteInput in = ByteInput.wrap(handle.readBytes());
    DT1Reader reader = new DT1Reader();
    return reader.readDt1(handle.name(), in);
  }

  @Test
  @DisplayName("data\\global\\tiles\\ACT1\\TOWN\\floor.dt1")
  public void floor() {
    DT1 dt1 = testDt1(Gdx.files.internal("test/floor.dt1"));
  }

  @Test
  @DisplayName("data\\global\\tiles\\expansion\\Town\\shrine.dt1")
  public void shrine() {
    DT1 dt1 = testDt1(Gdx.files.internal("test/shrine.dt1"));
  }

  @Test
  @DisplayName("data\\global\\tiles\\expansion\\Town\\trees.dt1")
  public void trees() {
    DT1 dt1 = testDt1(Gdx.files.internal("test/trees.dt1"));
  }

  /**
   * (run = row of pixels)
   *
   * Validates that block runs have correct runs.
   *
   * Isometric blocks are 32x16, but encoded using 1/2 that (so 16x16=256
   * pixels) since a square block only contains a diamond's worth of pixels
   * which is exactly half of the area of the square.
   */
  @Test
  public void dt1_block_iso_tables() {
    assertEquals(Tile.SUBTILE_HEIGHT, Block.ISO_X_LEN.length);
    assertEquals(Tile.SUBTILE_HEIGHT, Block.ISO_X_OFF.length);
    for (int i = 0; i < Tile.SUBTILE_HEIGHT; i++) {
      assertEquals(Tile.SUBTILE_WIDTH, Block.ISO_X_LEN[i] + Block.ISO_X_OFF[i] * 2);
    }
  }

  @Test
  @DisplayName("dt1 finder")
  public void find() {
    DT1Reader reader = new DT1Reader();
    String[] dt1s = getDt1s();
    for (String dt1 : dt1s) {
      FileHandle handle = Riiablo.mpqs.resolve(dt1);
      if (handle == null) {
        System.out.println("FileNotFound: " + dt1);
        continue;
      }

      try {
        ByteInput in = ByteInput.wrap(handle.readBytes());
        reader.readDt1(handle.name(), in);
      } catch (Throwable t) {
        System.out.println(ExceptionUtils.getRootCauseMessage(t));
        continue;
      }
    }
  }

  String[] getDt1s() {
    return new String[]{
        "data\\global\\tiles\\ACT1\\BARRACKS\\barracks.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Barracks\\barset.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Barracks\\basewall.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Barracks\\exitdn.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Barracks\\floor.dt1",
        "data\\global\\tiles\\ACT1\\BARRACKS\\gargtrap.dt1",
        "DATA\\GLOBAL\\Tiles\\Act1\\Barracks\\InvisWal.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Barracks\\objects.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Barracks\\stairup.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Barracks\\Torture.dt1",
        "DATA\\GLOBAL\\Tiles\\Act1\\Barracks\\Warp.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Catacomb\\andariel.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Catacomb\\Basewalls.dt1",
        "data\\global\\tiles\\ACT1\\CATACOMB\\Catacombs.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Catacomb\\Cathstr.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Catacomb\\Dwnstr.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Catacomb\\floor.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Catacomb\\Upstr.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Cathedrl\\Alter.dt1",
        "data\\global\\tiles\\ACT1\\CATHEDRL\\Cathedrl.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Cathedrl\\Coffers.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Cathedrl\\column.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Cathedrl\\Frescoes.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Cathedrl\\rugs.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Cathedrl\\Stained.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Caves\\Cave.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Caves\\Cavedr.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Court\\Archwall.dt1",
        "data\\global\\tiles\\ACT1\\COURT\\Court.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Court\\Floor.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Court\\Outwall.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Court\\Pboxwall.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Court\\Plants2.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Crypt\\Basewall.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Crypt\\Floor.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Graveyard\\graveyrd.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Monastry\\Facade.dt1",
        "DATA\\GLOBAL\\Tiles\\Act1\\Outdoors\\Blank.dt1",
        "data\\global\\tiles\\ACT1\\OUTDOORS\\border.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Outdoors\\Bridge.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Outdoors\\Cairn.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Outdoors\\Cliff1.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Outdoors\\Cliff2.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Outdoors\\Corner.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Outdoors\\Cottages.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Outdoors\\Fallen.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Outdoors\\Fence.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Outdoors\\Objects.dt1",
        "data\\global\\tiles\\ACT1\\OUTDOORS\\Outdoor1.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Outdoors\\pond.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Outdoors\\puddle.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Outdoors\\River.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Outdoors\\Ruin.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Outdoors\\Stones.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Outdoors\\stonewall.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Outdoors\\Swamp.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Outdoors\\tome.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Outdoors\\Tower.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Outdoors\\TowerB.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Outdoors\\TreeGroups.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Town\\Fence.dt1",
        "DATA\\GLOBAL\\Tiles\\Act1\\Town\\Floor.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Town\\Objects.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Town\\trees.dt1",
        "DATA\\GLOBAL\\TILES\\Act1\\Tristram\\town.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Arcane\\Sanctuary.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\BigCliff\\CliffLeft.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\BigCliff\\CliffMesa.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\BigCliff\\CliffRight.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\BigCliff\\Corner.dt1",
        "data\\global\\tiles\\ACT2\\BigCliff\\kings.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\BigCliff\\Stairs.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\BigCliff\\TalTombL.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\BigCliff\\TalTombR.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\BigCliff\\TombCor.dt1",
        "data\\global\\tiles\\ACT2\\BigCliff\\tombleft.dt1",
        "data\\global\\tiles\\ACT2\\BigCliff\\tombrght.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Maggot\\Den.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Maggot\\Entrance.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Maggot\\Floor.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Maggot\\Hole.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Outdoors\\Bone.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Outdoors\\Dune.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Outdoors\\Head.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Outdoors\\Mesa.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Outdoors\\Oasis.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Outdoors\\Palm.dt1",
        "data\\global\\tiles\\ACT2\\Outdoors\\path.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Outdoors\\Prickly.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Outdoors\\Rocks.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Outdoors\\Scrub.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Outdoors\\SmCliff.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Outdoors\\TombEnt.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Outdoors\\Village.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Outdoors\\Viper.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Outdoors\\Wagon.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Palace\\Cellar.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Palace\\CellFlr.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Palace\\Harem.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Palace\\HaremEnt.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Palace\\HaremFlr.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Palace\\HaremStair.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Palace\\InvisWal.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Palace\\Palace.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Palace\\Sub - Har.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Palace\\Sub - HarFlr.dt1",
        "data\\global\\tiles\\ACT2\\Palace\\wall.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Ruin\\Column.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Ruin\\Ground.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Sewer\\Chamb.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Sewer\\Items.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Sewer\\Radament.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Sewer\\Walls.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Tomb\\Columns.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Tomb\\Duriel.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Tomb\\Secret.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Tomb\\Serpent.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Tomb\\Stairs.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Tomb\\Statuerm.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Tomb\\Talrasha.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Tomb\\Things.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Tomb\\Tomb.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Tomb\\Tombsteps.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Tomb\\Treasure.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Town\\Build.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Town\\Canal.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Town\\Curbs.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Town\\Gate.dt1",
        "DATA\\GLOBAL\\Tiles\\Act2\\Town\\Ground.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Town\\Guard.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Town\\Shop.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Town\\Tavern.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Town\\Wagon.dt1",
        "DATA\\GLOBAL\\TILES\\Act2\\Town\\Walls.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Docktown\\Boat.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Docktown\\Bridge.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Docktown\\Docks.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Docktown\\Huts.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Docktown\\Market.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Docktown\\Pyramid.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Docktown\\RuinFlr.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Docktown\\RuinWall.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Docktown\\Shack.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Ground\\DarkGrass.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Ground\\DarkMud.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Ground\\DryMud.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Ground\\GreenMud.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Jungle\\DungEnt.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Jungle\\Dungeon.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Jungle\\Pygmy.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Jungle\\Ruin.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Jungle\\TreeGrp.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Jungle\\Trees.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Kurast\\Floors.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Kurast\\Huts.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Kurast\\Interior.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Kurast\\Roofs.dt1",
        "DATA\\GLOBAL\\Tiles\\ACT3\\Kurast\\sets.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Kurast\\Terraces.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Kurast\\Walls.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\River\\Pool.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\River\\RivBank.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Sewer\\Floors.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Sewer\\Walls.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Spider\\Lair.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Spider\\SpiderEnt.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Travincal\\Floors.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Travincal\\Gate.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Travincal\\Terraces.dt1",
        "DATA\\GLOBAL\\TILES\\Act3\\Travincal\\Walls.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Diab\\Bridge.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Diab\\Floor.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Diab\\Walls.dt1",
        "data\\global\\tiles\\ACT4\\Expansion\\specials.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Fort\\Exterior.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Fort\\ForeGate.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Fort\\Interior.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Fort\\Plaza.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Lava\\Extwalls.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Lava\\Floor.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Lava\\Floornew.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Lava\\Intwalls.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Lava\\Rocks.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Lava\\Specials.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Lava\\Walls.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Mesa\\Arch_Walls.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Mesa\\Brick_Walls.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Mesa\\Chain_Walls.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Mesa\\Dist_Wall.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Mesa\\Floor.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Mesa\\Inv_Wall.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Mesa\\Stairs.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Mesa\\Surf_Struct.dt1",
        "DATA\\GLOBAL\\TILES\\Act4\\Mesa\\Surf_Wall.dt1",
        "data\\global\\tiles\\ACT4\\Mesa\\weird.dt1",
        "data\\global\\tiles\\expansion\\BaalLair\\floor.dt1",
        "data\\global\\tiles\\expansion\\BaalLair\\throne.dt1",
        "data\\global\\tiles\\expansion\\BaalLair\\underflr.dt1",
        "data\\global\\tiles\\expansion\\BaalLair\\underwall.dt1",
        "data\\global\\tiles\\expansion\\BaalLair\\walls.dt1",
        "data\\global\\tiles\\expansion\\BaalLair\\worldstone.dt1",
        "data\\global\\tiles\\expansion\\Icecave\\exterior.dt1",
        "data\\global\\tiles\\expansion\\Icecave\\interior.dt1",
        "data\\global\\tiles\\expansion\\Icecave\\warps.dt1",
        "data\\global\\tiles\\expansion\\MountainTop\\columns.dt1",
        "data\\global\\tiles\\expansion\\MountainTop\\entrance.dt1",
        "data\\global\\tiles\\expansion\\MountainTop\\floor.dt1",
        "data\\global\\tiles\\expansion\\ruins\\furnish.dt1",
        "data\\global\\tiles\\expansion\\ruins\\walls.dt1",
        "data\\global\\tiles\\expansion\\Siege\\ascendors.dt1",
        "data\\global\\tiles\\expansion\\Siege\\barricade.dt1",
        "data\\global\\tiles\\expansion\\Siege\\battle.dt1",
        "data\\global\\tiles\\expansion\\Siege\\building.dt1",
        "data\\global\\tiles\\expansion\\Siege\\camp.dt1",
        "data\\global\\tiles\\expansion\\Siege\\cliff.dt1",
        "data\\global\\tiles\\expansion\\Siege\\fence.dt1",
        "data\\global\\tiles\\expansion\\Siege\\fortified.dt1",
        "data\\global\\tiles\\expansion\\Siege\\ground.dt1",
        "data\\global\\tiles\\expansion\\Siege\\hellgate.dt1",
        "data\\global\\tiles\\expansion\\Siege\\path.dt1",
        "data\\global\\tiles\\expansion\\Siege\\pens.dt1",
        "data\\global\\tiles\\expansion\\Siege\\pinetree.dt1",
        "data\\global\\tiles\\expansion\\Siege\\rockcliff.dt1",
        "data\\global\\tiles\\expansion\\Siege\\rockclifffloor.dt1",
        "data\\global\\tiles\\expansion\\Siege\\rocks.dt1",
        "data\\global\\tiles\\expansion\\Siege\\shrub.dt1",
        "data\\global\\tiles\\expansion\\Siege\\snow.dt1",
        "data\\global\\tiles\\expansion\\Siege\\special.dt1",
        "data\\global\\tiles\\expansion\\Siege\\spike.dt1",
        "data\\global\\tiles\\expansion\\Siege\\temptile.dt1",
        "data\\global\\tiles\\expansion\\Siege\\trees.dt1",
        "data\\global\\tiles\\expansion\\Siege\\trench.dt1",
        "data\\global\\tiles\\expansion\\specials.dt1",
        "data\\global\\tiles\\expansion\\Town\\blacksmith.dt1",
        "data\\global\\tiles\\expansion\\Town\\buildings.dt1",
        "data\\global\\tiles\\expansion\\Town\\buildingses.dt1",
        "data\\global\\tiles\\expansion\\Town\\clutter.dt1",
        "data\\global\\tiles\\expansion\\Town\\collision.dt1",
        "data\\global\\tiles\\expansion\\Town\\ground.dt1",
        "data\\global\\tiles\\expansion\\Town\\keepwall.dt1",
        "data\\global\\tiles\\expansion\\Town\\maingate.dt1",
        "data\\global\\tiles\\expansion\\Town\\otherwalls.dt1",
        "data\\global\\tiles\\expansion\\Town\\rkwall.dt1",
        "data\\global\\tiles\\expansion\\Town\\shrine.dt1",
        "data\\global\\tiles\\expansion\\Town\\tent.dt1",
        "data\\global\\tiles\\expansion\\Town\\trees.dt1",
        "data\\global\\tiles\\expansion\\Town\\walls.dt1",
        "data\\global\\tiles\\expansion\\Town\\walls2.dt1",
        "data\\global\\tiles\\expansion\\Town\\waypt.dt1",
        "data\\global\\tiles\\expansion\\Wildtemple\\entrance.dt1",
        "data\\global\\tiles\\expansion\\Wildtemple\\interior.dt1",
    };
  }
}
