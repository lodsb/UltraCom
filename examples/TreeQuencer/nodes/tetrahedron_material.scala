import main.scala.TreeQuencer.{NodeMaterial, app};
import org.mt4j.util.math.Tools3D;

new NodeMaterial(Tools3D.getGL(app)) {
  setAmbient(Array( 196/255f,77f/255f,88f/255f, 0.9f ));
  setDiffuse(Array( 196/255f,77f/255f,88f/255f, 0.9f ));
  setSpecular(Array( 196/255f,77f/255f,88f/255f, 0.9f ));
  setEmission(Array( 0.0f, 0.0f, 0.0f, 1f ));
  setShininess(80);
}