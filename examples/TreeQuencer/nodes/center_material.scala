import main.scala.TreeQuencer.{NodeMaterial, app};
import org.mt4j.util.math.Tools3D;

new NodeMaterial(Tools3D.getGL(app)) {
  println("I am within NodeMaterial...........................................")
  setAmbient(Array( 0.8f, 0.8f, 0.8f, 1f ));
  setDiffuse(Array( 0.8f, 0.8f, 0.8f, 1f ));
  setSpecular(Array( 0.8f, 0.8f, 0.8f, 1f ))
  setEmission(Array( 0.0f, 0.0f, 0.0f, 1f ));;
  setShininess(80);
}
