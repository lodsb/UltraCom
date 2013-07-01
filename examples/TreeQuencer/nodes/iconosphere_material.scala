import main.scala.TreeQuencer.{NodeMaterial, app};
import org.mt4j.util.math.Tools3D;

new NodeMaterial(Tools3D.getGL(app)) {
  setAmbient(Array( 0f, 0f, 1f, 0.95f));
  setDiffuse(Array( 0f, 0f, 1f, 0.95f ));
  setSpecular(Array( 0f, 0f, 1f, 0.95f ))
  setEmission(Array( 0.0f, 0.0f, 0.0f, 1f ));;
  setShininess(80);
}