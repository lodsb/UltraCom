import main.scala.TreeQuencer.{NodeMaterial, app};
import org.mt4j.util.math.Tools3D;

new NodeMaterial(Tools3D.getGL(app)) {
  setAmbient(Array( 1f, 0f, 0f, 1f ));
  setDiffuse(Array( 1f, 0f, 0f, 1f ));
  setSpecular(Array( 1f, 0f, 0f, 1f ));
  setEmission(Array( 0.0f, 0.0f, 0.0f, 1f ));
  setShininess(80);
}