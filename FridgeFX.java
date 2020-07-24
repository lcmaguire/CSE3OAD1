import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.scene.control.*;
import javafx.scene.layout.*;


import java.util.*;
import java.io.*;
import javafx.collections.*;
import javafx.collections.transformation.*;
import javafx.scene.control.cell.*;
import javafx.beans.property.*;

//overide init method file not found exception

/*
	Liam Maguire 18344533
	18344533@students.latrobe.edu.au
	CSE3OAD
*/

public class FridgeFX extends Application {

	// used as ChoiceBox value for filter
	public enum FILTER_COLUMNS {
		ITEM,
		SECTION,
		BOUGHT_DAYS_AGO
	};
	
	// Table View Column widths
	public static final int ID_COL_MIN_WIDTH = 50;
	public static final int ITEM_COL_MIN_WIDTH = 200;
	public static final int QTY_COL_MIN_WIDTH = 50;
	public static final int SECTION_COL_MIN_WIDTH = 100;
	public static final int DAYS_AGO_COL_WIDTH = 150;
	
	// the data source controller
	private FridgeDSC fridgeDSC;
	private ObservableList<Grocery> tableData;
	
	public void init() throws Exception {
		// creates and connects to fridgeDSC
		fridgeDSC = new FridgeDSC();
		fridgeDSC.connect();
		

	}

	public void start(Stage stage) throws Exception {


		build(stage);
		stage.setTitle("What's in my fridge v1.0");
		stage.show();
		
		// catches all exceptions and displays error warning
		Thread.currentThread().setUncaughtExceptionHandler((thread, exception) ->
		{	
			System.out.println("ERROR: " + exception);
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText(null);
			alert.setContentText("Exception: " + exception.getMessage() + "\n" + exception);
			
			alert.showAndWait();
			
		}
		);

	}

	public void build(Stage stage) throws Exception {

		TableView<Grocery> tableView = makeTable();
		HBox filterPane;
		try{
		filterPane = makeFilterPane(tableView);
		}
		catch(Exception exception){
			throw new RuntimeException(exception.getMessage());
		}
		
		// set up add pane
		VBox addPane = makeAddPane();
		addPane.setVisible(false);

		// set up buttons
		Button addButton = new Button("ADD");
		Button updateButton = new Button("UPDATE ONE");
		Button deleteButton = new Button("DELETE");
		
		// sets add pane visible and then runs code from within
		addButton.setOnAction(e ->
		{	
			addPane.setVisible(true);
		});
		
		// if not null checks if can update grocery, then asks user before updating 
		updateButton.setOnAction(e ->
		{
			try{
				Grocery temp = tableView.getSelectionModel().getSelectedItem();
						
				// if not null check go to dsc to decrement, then if successful find object and decrement before showing to table
				if(temp != null){
					if(temp.getQuantity() > 1){
						if(confirmation("Update Grocery?")){
							temp = fridgeDSC.useGrocery(temp.getId());
							
							for(Grocery p: tableData)
							{
								if(p.getId() == temp.getId())
								{
									p.updateQuantity();
									// Refresh the column to see the change
									// (This is a work around)
									tableView.getColumns().get(0).setVisible(false);
									tableView.getColumns().get(0).setVisible(true);
									System.out.println("Updated " +  temp);
									break;
								}
							}
						}
					}else{
						throw new RuntimeException("There is only 1 " + temp.getItemName() + " bought on " + temp.getDateStr() + "- Use DELETE instead.");
					}
				} else{
					// value is null no item selected
					throw new RuntimeException("No Item Selected to Update");
				}
			} catch(Exception exception){			
				throw new RuntimeException(exception.getMessage());
			}
		});
		
		// if selected value isnt null ask user if wants to delete before deleting
		deleteButton.setOnAction(e ->
		{
			try{
				Grocery temp = tableView.getSelectionModel().getSelectedItem();
				int id;
				if(temp != null){
				
					// creates confirmation alert if positive removes from db then tableview
					if(confirmation("Delete Grocery?")){
							id = fridgeDSC.removeGrocery(temp.getId());
							if(id == 1){
							tableData.remove(temp);
							System.out.println("deleted "  + temp);
							}
						}
				} else{
					// value is null no item selected
					throw new RuntimeException("No Item Selected To Delete");
				}
			} catch(Exception exception){
					
				throw new RuntimeException(exception.getMessage());
			}
		});
		
		HBox h2 = new HBox();
		
		h2.getChildren().addAll(addButton, updateButton, deleteButton);
		
		VBox root = new VBox(
						filterPane,
						tableView, 
						h2,
						addPane
			);
	
		root.setStyle(
			"-fx-font-size: 16;" +
			"-fx-alignment: center;" + 
			"-fx-padding: 5;"
		);
		Scene scene = new Scene(root);
		
		stage.setScene(scene);
	}
	
	// creates tableview and gets data from database
	
	public TableView<Grocery> makeTable() throws Exception {
		
		tableData = FXCollections.observableArrayList(fridgeDSC.getAllGroceries());
		
		// Define table columns
		TableColumn<Grocery, String> idColumn = new TableColumn<Grocery, String>("Id");
		TableColumn<Grocery, String> itemNameColumn = new TableColumn<Grocery, String>("Item");
		TableColumn<Grocery, Integer> quantityColumn = new TableColumn<Grocery, Integer>("QTY");
		TableColumn<Grocery, String> sectionColumn = new TableColumn<Grocery, String>("Section");
		TableColumn<Grocery, String> daysAgoColumn = new TableColumn<Grocery, String>("Bought");
		
		// change tags to match wanted value
		
		idColumn.setCellValueFactory( new PropertyValueFactory<Grocery, String>("Id"));
		itemNameColumn.setCellValueFactory( new PropertyValueFactory<Grocery, String>("itemName"));
		quantityColumn.setCellValueFactory( new PropertyValueFactory<Grocery, Integer>("quantity"));
		sectionColumn.setCellValueFactory( new PropertyValueFactory<Grocery, String>("Section"));
		daysAgoColumn.setCellValueFactory( new PropertyValueFactory<Grocery, String>("daysAgo"));

		// Create the table view and add table columns to it
		TableView<Grocery> tableView = new TableView<Grocery>();
		
		tableView.getColumns().add(idColumn);
		tableView.getColumns().add(itemNameColumn);
		tableView.getColumns().add(quantityColumn);
		tableView.getColumns().add(sectionColumn);
		tableView.getColumns().add(daysAgoColumn);

		//	Attach table data to the table view
		tableView.setItems(tableData);

		idColumn.setMinWidth(ID_COL_MIN_WIDTH);
		itemNameColumn.setMinWidth(ITEM_COL_MIN_WIDTH);
		quantityColumn.setMinWidth(QTY_COL_MIN_WIDTH);
		sectionColumn.setMinWidth(SECTION_COL_MIN_WIDTH);
		daysAgoColumn.setMinWidth(DAYS_AGO_COL_WIDTH);
		
		return tableView;	
	}
	
	// creates filterBox and handles text filtering, changing of choicebox
	private HBox makeFilterPane(TableView<Grocery> tableView) throws Exception{
		
		TextField tf2 = new TextField("");
		tf2.setEditable(true);
		 
		Label choiceLabel = new Label("Filter By:");
		// choice box
		ChoiceBox<String> choices = new ChoiceBox<String>();
		choices.getItems().addAll(FILTER_COLUMNS.ITEM.toString(),FILTER_COLUMNS.SECTION.toString(),FILTER_COLUMNS.BOUGHT_DAYS_AGO.toString());
		// Set default value
		choices.setValue(FridgeFX.FILTER_COLUMNS.ITEM.toString());

		// check box
		CheckBox cb1 = new CheckBox("Show Expire Only");
		cb1.setSelected(false);
		cb1.setDisable(true);
		
		// on change listener
		choices.getSelectionModel().selectedItemProperty().addListener(
		// ChangeListener
		(observableValue, oldValue, newValue) ->
		{		
			// if newvalue is bought_days_ago set bought days ago to be selectable else disable and de select it
			if(newValue != "BOUGHT_DAYS_AGO"){
				cb1.setSelected(false);
				cb1.setDisable(true);
			}else{
				//cb1.setSelected(false);
				cb1.setDisable(false);
			}
			
			// clears the text field
			tf2.clear();
			
			// sets focus on textField
			tf2.requestFocus();
			
		});
		
		// on action clears text field and focuses on it
		cb1.setOnAction((e) ->{
			
				// clears text field
				tf2.clear();
			
				// sets focus on textField
				tf2.requestFocus();
			}
		);
		
		// Make filteredList and Sorted List then bind
		FilteredList<Grocery> filteredList = new FilteredList<>(tableView.getItems(), p -> true);

		SortedList<Grocery> sortedList = new SortedList<>(filteredList);
		sortedList.comparatorProperty().bind(tableView.comparatorProperty());
		tableView.setItems(sortedList);
				
		// filters tableview based upon input and selected choice
		tf2.textProperty()
	  		.addListener((observable, oldValue, newValue) ->
	  		{
				//filter type 
				if(choices.getValue().equalsIgnoreCase("ITEM")){
				filteredList.setPredicate(grocery ->
					{
						if (newValue == null || newValue.isEmpty())
							{
								return true;
							}

							// Compare product's name with filter text
							// If match, return true. Otherwise return false
							
							String filterString = newValue.toUpperCase();
							
							return grocery.getItemName().toUpperCase().contains(filterString);
			
					});
				} else if (choices.getValue().equalsIgnoreCase("SECTION")){
					
					filteredList.setPredicate(grocery ->
					{
						if (newValue == null || newValue.isEmpty())
							{
								return true;
							}

							// Compare product's name with filter text
							// If match, return true. Otherwise return false
							
							String filterString = newValue.toUpperCase();
							
							return grocery.getSection().toString().toUpperCase().contains(filterString);

					});
					
				}else if(choices.getValue().equalsIgnoreCase("BOUGHT_DAYS_AGO") && cb1.isSelected()){
										 
					filteredList.setPredicate(grocery ->
					{
						if (newValue == null || newValue.isEmpty())
							{
								return grocery.getItem().canExpire();
								//return true;
							}

							// Compare product's name with filter text
							// If match, return true. Otherwise return false
				
							int days =0;
							try{
								
								if(newValue.matches("[0-9]+")){
								days = Integer.parseInt(newValue);
								}else{
									throw new RuntimeException("Input Must be int");
								}
							
								return grocery.getItem().canExpire() && FridgeDSC.calcDaysAgo(grocery.getDate()) >= days;
								
								}catch(Exception exception){
								//filteredList.removeAll();
								tf2.clear();
								//sortedList.clear();
								tableView.setItems(tableData);
								resetTableView(filteredList, sortedList, tableView);
								
								throw new RuntimeException(exception.getMessage());
								
							}
							
					});
									
				}else if(choices.getValue().equalsIgnoreCase("BOUGHT_DAYS_AGO") && !cb1.isSelected()){
										 
					filteredList.setPredicate(grocery ->
					{	
						if (newValue == null || newValue.isEmpty())
							{
								return true;
							}

							// Compare product's name with filter text
							// If match, return true. Otherwise return false
							
							int days =0;
							try{
								
								if(newValue.matches("[0-9]+")){
								days = Integer.parseInt(newValue);
								}else{
									throw new RuntimeException("Must be int");
								}
								return FridgeDSC.calcDaysAgo(grocery.getDate()) >= days;
							}catch(Exception exception){
								
								tf2.clear();
								
								tableView.setItems(tableData);
								resetTableView(filteredList, sortedList, tableView);
								throw new RuntimeException("Input Must Be Int");
							}
					});	
				}	
		  });
		  
		// creats hbox before returning 		  
		HBox pane = new HBox(tf2, choiceLabel, choices, cb1);
		pane.setStyle("-fx-alignment: center;" + "-fx-spacing: 20;");
		
		return pane;
	}
	
	// i had an error with the size of the tableview and sortedList doubling when an invalid input was entered for the days ago section, this fixes it
	// re assigns the tableView to the table data reAssigns the SortedList to the Filter List
	public void resetTableView(FilteredList<Grocery> filteredList, SortedList<Grocery> sortedList, TableView<Grocery> tableView ){
		
		//sortedList.clear();
		tableView.setItems(tableData);
		sortedList = new SortedList<>(filteredList);
		sortedList.comparatorProperty().bind(tableView.comparatorProperty());
		tableView.setItems(sortedList);
		
		sortedList = new SortedList<>(filteredList);
		sortedList.comparatorProperty().bind(tableView.comparatorProperty());
		tableView.setItems(sortedList);	
	}

	public VBox makeAddPane() throws Exception{
		
		ComboBox<Item> combo = new ComboBox<Item>();
		combo.getItems().addAll(fridgeDSC.getAllItems());
		combo.setVisibleRowCount(4);
		
		Label comboLabel = new Label("Item");
		comboLabel.setGraphic(combo);
		comboLabel.setStyle("-fx-content-display: BOTTOM");

		// choice box
		ChoiceBox<String> choices = new ChoiceBox<String>();
		choices.getItems().addAll(FridgeDSC.SECTION.FREEZER.toString(),FridgeDSC.SECTION.MEAT.toString(),FridgeDSC.SECTION.COOLING.toString(), FridgeDSC.SECTION.CRISPER.toString());
		
		// label for choicebox
		Label choicesLabel = new Label("Section");
		choicesLabel.setGraphic(choices);
		choicesLabel.setStyle("-fx-content-display: BOTTOM");
		
		//textfield and label
		TextField quantityTextField = new TextField("");
		quantityTextField.setEditable(true);
		
		Label quantityLabel = new Label("Quantity");
		quantityLabel.setGraphic(quantityTextField);
		quantityLabel.setStyle("-fx-content-display: BOTTOM");
		
		//creates hbox 
		HBox hb = new HBox(combo, comboLabel, choices, choicesLabel, quantityTextField, quantityLabel);
		hb.setStyle("-fx-spacing: 20;");
		
		// buttons for saving and clearing
		Button saveButton = new Button("SAVE");
		Button cancelButton = new Button("CLEAR");
		
		HBox hb2 = new HBox(cancelButton, saveButton);
		hb2.setStyle("-fx-alignment: center;");
		
		//adds to vbox
		VBox vbox= new VBox(hb, hb2);
		
		saveButton.setOnAction(e ->
		{		
			String name;
			int qty;
			FridgeDSC.SECTION section;
			try{
				name = combo.getValue().getName();
				qty = Integer.parseInt(quantityTextField.getText().trim());
				section = FridgeDSC.SECTION.valueOf(choices.getValue());
				
				// gets prompt for confirmation if true adds to db and tableview
					if(confirmation("Add Grocery?")){
						int result = fridgeDSC.addGrocery(name, qty, section);
				
						// if >= than 0 add to db successful and add to tableView
						if(result >= 0){
								//clears add section and hides before adding to tableview
								combo.setValue(null);
								choices.setValue(null);
								quantityTextField.clear();
								vbox.setVisible(false);
								tableData.add(fridgeDSC.searchGrocery(result));
								System.out.println("Saved to DB");
						}
					}
				
			} catch (Exception exception){
				throw new RuntimeException(exception.getMessage());
			}	
		});
		
		cancelButton.setOnAction(e ->
		{
			// hides and clears section
			combo.setValue(null);
			choices.setValue(null);
			quantityTextField.clear();
			
			vbox.setVisible(false);
			System.out.println("Canceled Add");

		});
				
		return vbox;
	}
	
	// method that takes in a message displas confirmationAlert if ok returns true else returns false
	public boolean confirmation(String message){
		
				Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
				alert.setHeaderText("Confirmation");
				alert.setContentText("Are You Sure? " + message);
				
				Optional<ButtonType> button = alert.showAndWait();
				if (button.isPresent())
				{
					if(button.get() == ButtonType.OK){
						return true;
					}else{
						return false;
					}
				}
				return false;
	}
	
	public void stop() throws Exception {
		// disconnect from db
		fridgeDSC.disconnect();
			
	}	
}