import main.scala.TreeQuencer.{NodeMaterial, app};
import org.mt4j.util.math.Tools3D;

new NodeMaterial(Tools3D.getGL(app)) {
  setAmbient(Array( 78f/255f,205f/255f,196f/255f, 0.95f ));
  setDiffuse(Array( 78f/255f,205f/255f,196f/255f, 0.95f ));
  setSpecular(Array( 78f/255f,205f/255f,196f/255f, 0.9f ));
  setEmission(Array( 0.0f, 0.0f, 0.0f, 1f ));
  setShininess(80);
}