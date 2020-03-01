package com.example.rajesh.customarfragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.ar.core.*;
import com.google.ar.sceneform.*;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.collision.Box;
import com.google.ar.sceneform.collision.Ray;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.*;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import android.graphics.Point;

public class SecondActivity extends AppCompatActivity implements Scene.OnUpdateListener, Scene.OnPeekTouchListener{
    ModelRenderable cube;

    private Anchor anchor1;
    private AnchorNode lastAnchorNode,firstAnchorNode;
    private AnchorNode anchorNode;
    ArFragment arFragment;
    private Material material;
    private Stroke currentStroke;
    private final ArrayList<Stroke> strokes = new ArrayList<>();
    private ModelRenderable lineRenderable;
    private ViewRenderable viewRenderable;
    private static final double MIN_OPENGL_VERSION = 3.0;
    private static final float DRAW_DISTANCE = 0.13f;
    private static final Color WHITE = new Color(android.graphics.Color.WHITE);
    private static final Color RED = new Color(android.graphics.Color.RED);
    private static final Color GREEN = new Color(android.graphics.Color.GREEN);
    private static final Color BLUE = new Color(android.graphics.Color.BLUE);
    private static final Color BLACK = new Color(android.graphics.Color.BLACK);
    private MotionEvent motionEvent;
    private TapHelper tapHelper;
    LinkedList<HitResult> hitResults=new LinkedList<>();

    private final BlockingQueue<MotionEvent> queuedSingleTaps = new ArrayBlockingQueue<>(16);
    Button mDrawBTN;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_activity);
        tapHelper=new TapHelper(this);
        mDrawBTN=(Button)findViewById(R.id.btn_draw);
        mDrawBTN.setVisibility(View.VISIBLE);




        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        mDrawBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Frame frame = arFragment.getArSceneView().getArFrame();
                Point point = getScreenCenter();
                if (frame != null) {
                    List<HitResult> hits = frame.hitTest((float) point.x, (float) point.y);

                    for (int i = 0; i < hits.size(); i++) {
                        Trackable trackable = hits.get(i).getTrackable();
                        if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hits.get(i).getHitPose())) {
                            addObjectNew(Uri.parse("andy.sfb"), hits.get(i));
                            Anchor anchor= hits.get(i).createAnchor();
                            AnchorNode currentAnchorNod=new AnchorNode(anchor);
                            if(lastAnchorNode!=null){
                                currentAnchorNod.setParent(arFragment.getArSceneView().getScene());
                                Vector3 point1, point2;
                                point1 = lastAnchorNode.getWorldPosition();
                                point2 = currentAnchorNod.getWorldPosition();

    /*
        First, find the vector extending between the two points and define a look rotation
        in terms of this Vector.
    */
                                final Vector3 difference = Vector3.subtract(point1, point2);
                                final Vector3 directionFromTopToBottom = difference.normalized();
                                final Quaternion rotationFromAToB =
                                        Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
                                MaterialFactory.makeOpaqueWithColor(getApplicationContext(), new Color(0, 255, 244))
                                        .thenAccept(
                                                material -> {
                            /* Then, create a rectangular prism, using ShapeFactory.makeCube() and use the difference vector
                                   to extend to the necessary length.  */

                                                    ViewRenderable.builder()
                                                            .setView(SecondActivity.this, R.layout.model_view)
                                                            .build()
                                                            .thenAccept(renderable -> viewRenderable = renderable);
                                                    material.setTexture(" value ",null);

                                                    ModelRenderable model = ShapeFactory.makeCube(
                                                            new Vector3(.01f, .01f, difference.length()),
                                                            Vector3.zero(), material);


                            /* Last, set the world rotation of the node to the rotation calculated earlier and set the world position to
                                   the midpoint between the given points . */
                                                    Node node = new Node();
                                                    node.setParent(currentAnchorNod);
                                                    node.setName(" Value ");
                                                    node.setRenderable(model);
                                                    node.setWorldPosition(Vector3.add(point1, point2).scaled(.5f));
                                                    node.setWorldRotation(rotationFromAToB);
                                                }
                                        );

                            }
                            lastAnchorNode=currentAnchorNod;


                        }
                    }
                }
            }
        });


        MaterialFactory.makeOpaqueWithColor(this, WHITE)
                .thenAccept(material1 -> material = material1.makeCopy())
                .exceptionally(
                        throwable -> {
                            displayError(throwable);
                            throw new CompletionException(throwable);
                        });

        MaterialFactory.makeOpaqueWithColor(arFragment.getContext(), RED)
                .thenAccept(material1 -> material = material1.makeCopy())
                .exceptionally(
                        throwable -> {
                            displayError(throwable);
                            throw new CompletionException(throwable);
                        });

        arFragment.getArSceneView().getScene().setOnTouchListener(tapHelper);

      //  arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);
     /*   arFragment.getArSceneView().getScene().addOnUpdateListener(this);
        arFragment.getArSceneView().getScene().setOnTouchListener(this);
*/

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {



                    MaterialFactory.makeTransparentWithColor(this, new Color(1, 0, 0, 0.5f))
                            .thenAccept(material ->
                                    cube = ShapeFactory.makeCube(new Vector3(0.01f, 0.01f, 0.01f), new Vector3(0, 0, 0), material));

/*

                    MaterialFactory.makeOpaqueWithColor(this,RED)
                            .thenAccept(material1 -> {
                                addNodeToScene(arFragment,hitResult.createAnchor(),ShapeFactory.makeCube(
                                        new Vector3(.2f,.2f,.2f),new Vector3(.2f,.2f,.2f),material1)
                                );
                            });


                       // addObjectNew(Uri.parse("andy.sfb"), hitResult);
                        hitResults.add(hitResult);
                        //findDistance(hitResult, plane, motionEvent);
                       // addLineBetweenHits(hitResult, plane, motionEvent);
*/


                }
        );

    }

    private void findDistance(HitResult hitResult,Plane plane,MotionEvent motionEvent){


       Anchor anchor2;
         if(anchor1==null){
             anchor1=hitResult.createAnchor();
         }else {
             anchor2=hitResult.createAnchor();
             double distanceCm = getMetersBetweenAnchors(anchor1,anchor2);
             Toast.makeText(SecondActivity.this," Distance : "+distanceCm,Toast.LENGTH_SHORT).show();

         }


    }

    private double getMetersBetweenAnchors(Anchor anchor1, Anchor anchor2) {
        float[] distance_vector = anchor1.getPose().inverse()
                .compose(anchor2.getPose()).getTranslation();
        float totalDistanceSquared = 0;
        for(int i=0; i<3; ++i)
            totalDistanceSquared += distance_vector[i]*distance_vector[i];
        return Math.sqrt(totalDistanceSquared);
    }

    private double getDistanceMeters(Pose pose0, Pose pose1) {

        float distanceX = pose0.tx() - pose1.tx();
        float distanceY = pose0.ty() - pose1.ty();
        float distanceZ = pose0.tz() - pose1.tz();

        return Math.sqrt(distanceX * distanceX +
                distanceY * distanceY +
                distanceZ * distanceZ);
    }

    private void placeObject(ArFragment fragment, Anchor anchor, Uri model) {

        CompletableFuture<Void> renderableFuture =
                ModelRenderable.builder()
                        .setSource(fragment.getContext(), model)
                        .build()
                        .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable))
                        .exceptionally((throwable -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage(throwable.getMessage())
                                    .setTitle("Codelab error!");
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return null;
                        }));
    }
    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable) {
       /* if(lastAnchorNode==null){
            lastAnchorNode=new AnchorNode(anchor);
        }*/
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
        node.setLocalScale(new Vector3(0.1f, 0.1f, 0.1f));
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
        Box box = (Box) node.getRenderable().getCollisionShape();
        Vector3 vector3 = box.getSize();
        Log.d(" width and height ",vector3.length()+" "+vector3.x+" "+ vector3.y+" "+ vector3.z);
    }

    private void addObjectNew(Uri model,HitResult hit){
        Trackable trackable = hit.getTrackable();
        if (trackable instanceof Plane &&
                ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
            placeObject(arFragment, hit.createAnchor(), model);

        }

    }

    private void addLineBetweenHits(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);

// Code to insert object probably happens here

        if (lastAnchorNode != null) {
            Vector3 point1, point2;
            point1 = lastAnchorNode.getWorldPosition();
            point2 = anchorNode.getWorldPosition();
            Node line = new Node();
            Color color = new Color(getResources().getColor(R.color.colorAccent, null));

    /* First, find the vector extending between the two points and define a look rotation in terms of this
        Vector. */

            final Vector3 difference = Vector3.subtract(point1, point2);
            final Vector3 directionFromTopToBottom = difference.normalized();
            final Quaternion rotationFromAToB =
                    Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());

            final Renderable[] lineRenderable = new Renderable[1];

    /* Then, create a rectangular prism, using ShapeFactory.makeCube() and use the difference vector
       to extend to the necessary length.  */

            MaterialFactory.makeOpaqueWithColor(this, color)
                    .thenAccept(
                            material -> {
                                lineRenderable[0] = ShapeFactory.makeCube(new Vector3(.01f, .01f, difference.length()),
                                        Vector3.zero(), material);
                            });

    /* Last, set the world rotation of the node to the rotation calculated earlier and set the world position to
       the midpoint between the given points . */
            line.setParent(anchorNode);
            line.setRenderable(lineRenderable[0]);
            line.setWorldPosition(Vector3.add(point1, point2).scaled(.5f));
            line.setWorldRotation(rotationFromAToB);
        }

        lastAnchorNode = anchorNode;

        }

    public void lineBetweenPoints(Vector3 point1, Vector3 point2,AnchorNode anchorNode) {

        Node lineNode = new Node();

   /* First, find the vector extending between the two points and define a look rotation in terms of this
        Vector. */

        final Vector3 difference = Vector3.subtract(point1, point2);
        final Vector3 directionFromTopToBottom = difference.normalized();
        final Quaternion rotationFromAToB =
                Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());

   /* Then, create a rectangular prism, using ShapeFactory.makeCube() and use the difference vector
         to extend to the necessary length.  */

        MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.WHITE))
                .thenAccept(
                        material -> {
                            lineRenderable = ShapeFactory.makeCube(new Vector3(.01f, .01f, difference.length()),
                                    Vector3.zero(), material);
                        });

   /* Last, set the local rotation of the node to the rotation calculated earlier and set the local position to
       the midpoint between the given points . */

        lineNode.setParent(anchorNode);
        lineNode.setRenderable(lineRenderable);
        lineNode.setLocalPosition(Vector3.add(point1, point2).scaled(.5f));
        lineNode.setLocalRotation(rotationFromAToB);

    }

    @Override
    public void onPeekTouch(HitTestResult hitTestResult, MotionEvent motionEvent) {
        this.motionEvent=motionEvent;

    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        com.google.ar.core.Camera camera = arFragment.getArSceneView().getArFrame().getCamera();
        if (camera.getTrackingState() == TrackingState.TRACKING) {
            arFragment.getPlaneDiscoveryController().hide();
        }

    }



    private void displayError(Throwable throwable) {
        Log.e("Second Activity", "Unable to create material", throwable);
        Toast toast = Toast.makeText(this, "Unable to create material", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
    private void drawView(){
        if(hitResults!=null){
            for(HitResult hitResult:hitResults){
                Anchor anchor=hitResult.createAnchor();
                AnchorNode currentAnchorNod=new AnchorNode(anchor);
                if(lastAnchorNode!=null){
                    currentAnchorNod.setParent(arFragment.getArSceneView().getScene());
                    Vector3 point1, point2;
                    point1 = lastAnchorNode.getWorldPosition();
                    point2 = currentAnchorNod.getWorldPosition();

    /*
        First, find the vector extending between the two points and define a look rotation
        in terms of this Vector.
    */
                    final Vector3 difference = Vector3.subtract(point1, point2);
                    final Vector3 directionFromTopToBottom = difference.normalized();
                    final Quaternion rotationFromAToB =
                            Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
                    MaterialFactory.makeOpaqueWithColor(getApplicationContext(), new Color(0, 255, 244))
                            .thenAccept(
                                    material -> {
                            /* Then, create a rectangular prism, using ShapeFactory.makeCube() and use the difference vector
                                   to extend to the necessary length.  */

                                        ViewRenderable.builder()
                                                .setView(this, R.layout.model_view)
                                                .build()
                                                .thenAccept(renderable -> viewRenderable = renderable);
                                        material.setTexture(" value ",null);

                                        ModelRenderable model = ShapeFactory.makeCube(
                                                new Vector3(.01f, .01f, difference.length()),
                                                Vector3.zero(), material);


                            /* Last, set the world rotation of the node to the rotation calculated earlier and set the world position to
                                   the midpoint between the given points . */
                                        Node node = new Node();
                                        node.setParent(currentAnchorNod);
                                        node.setName(" Value ");
                                        node.setRenderable(model);
                                        node.setWorldPosition(Vector3.add(point1, point2).scaled(.5f));
                                        node.setWorldRotation(rotationFromAToB);
                                    }
                            );

                }
                lastAnchorNode=currentAnchorNod;

            }
        }
    }

    private Point getScreenCenter() {
        View vw = findViewById(android.R.id.content);
        return new Point(vw.getWidth() / 2, vw.getHeight() / 2);
    }
}


