package network.programing.client.model;

import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

/**
 * A Class for Rendering users images / name on the online user list.
 */
public class CellRenderer implements Callback<ListView<String>,ListCell<String>>{
    @Override
    public ListCell<String> call(ListView<String> p) {

        ListCell<String> cell = new ListCell<String>(){

            @Override
            protected void updateItem(String user, boolean bln) {
                super.updateItem(user, bln);
                setGraphic(null);
                setText(null);
                if (user != null) {
                    HBox hBox = new HBox();

                    Text name = new Text("   " + user);
                    ImageView pictureImageView = new ImageView();
                    Image image = new Image(getClass().getClassLoader().getResource("avatar.png").toString(),50,50,true,true);
                    pictureImageView.setImage(image);

                    hBox.getChildren().addAll(pictureImageView, name);
                    hBox.setAlignment(Pos.CENTER_LEFT);

                    setGraphic(hBox);
                }
            }
        };
        return cell;
    }
}
