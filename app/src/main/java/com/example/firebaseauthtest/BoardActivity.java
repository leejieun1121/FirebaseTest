package com.example.firebaseauthtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class BoardActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<ImageDTO> list = new ArrayList<>();
    private List<String> uidLists = new ArrayList<>();
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        recyclerView = findViewById(R.id.rv_board);
        BoardRecyclerViewAdapter boardRecyclerViewAdapter = new BoardRecyclerViewAdapter();
        recyclerView.setAdapter(boardRecyclerViewAdapter);
        firebaseDatabase = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        firebaseDatabase.getReference().child("images").addValueEventListener(new ValueEventListener() {
            //옵저버패턴 Pattern
            //데이터가 바뀔때마다 넘어옴 -> 자동 새로고침
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                uidLists.clear();
                //images의 자식 노드들을 순회함 !
                for(DataSnapshot dataSnapshot :snapshot.getChildren()){
                    //순회한 데이터를 ImageDTO 클래스 형으로 받아옴 !
                    ImageDTO imageDTO = dataSnapshot.getValue(ImageDTO.class);
                    Log.d("tag",dataSnapshot.getValue().toString());
                    Log.d("tag",imageDTO.description);
                    Log.d("tag",imageDTO.imageUrl);
                    list.add(imageDTO);
                    uidLists.add(dataSnapshot.getKey());

                }
                //데이터가 변경되었으니 꼭 해주기
                boardRecyclerViewAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    class BoardRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board,parent,false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((CustomViewHolder) holder).tvTitle.setText(list.get(position).title);
            ((CustomViewHolder) holder).tvDescription.setText(list.get(position).description);
            Glide.with(holder.itemView.getContext()).load(list.get(position).imageUrl)
                    .into(((CustomViewHolder) holder).imgContent);
            ((CustomViewHolder) holder).tvLikeCount.setText(list.get(position).likeCount+"");

            ((CustomViewHolder) holder).imgLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //게시물하나당 키를 가지고 있음 타고 들어가서 해당 게시물을 알려줌 -> 좋아요 클릭 함수 발생
                    onLikeClicked(firebaseDatabase.getReference().child("images").child(uidLists.get(position)));
                }
            });

            //해시맵의 유저 아이디가 존재한다면 좋아요 누른거니까 채워진 하트
            if (list.get(position).imgLike.containsKey(auth.getCurrentUser().getUid())) {
                Glide.with(holder.itemView).load(R.drawable.baseline_favorite_black_18dp)
                        .into(((CustomViewHolder) holder).imgLike);
            }else{
                Glide.with(holder.itemView).load(R.drawable.baseline_favorite_border_black_18dp)
                        .into(((CustomViewHolder) holder).imgLike);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        private void onLikeClicked(DatabaseReference postRef) {
            //트랜잭션 -> 동시 수정 데이터 다루는 경우 (즐겨찾기,좋아요) 변화 겹치지 않도록
            postRef.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    ImageDTO imgeDTO = mutableData.getValue(ImageDTO.class);
                    if (imgeDTO == null) {
                        return Transaction.success(mutableData);
                    }
                    //imgLike해시맵 -> 해당 게시물에 좋아요 누르면 좋아요 누른 유저의 아이디가 저장됨
                    //해시맵의 해당 유저 아이디가 존재한 상태에서 클릭했을때 -> 좋아요 해지, count-1
                    if (imgeDTO.imgLike.containsKey(auth.getCurrentUser().getUid())) {
                        // Unstar the post and remove self from stars
                        imgeDTO.likeCount = imgeDTO.likeCount - 1;
                        imgeDTO.imgLike.remove(auth.getCurrentUser().getUid());
                    } else {
                        // Star the post and add self to stars
                        imgeDTO.likeCount = imgeDTO.likeCount + 1;
                        imgeDTO.imgLike.put(auth.getCurrentUser().getUid(), true);
                    }

                    // Set value and report transaction success
                    mutableData.setValue(imgeDTO);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean committed,
                                       DataSnapshot currentData) {
                    // Transaction completed
                    Log.d("tag", "postTransaction:onComplete:" + databaseError);
                }
            });
        }

    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView imgContent;
        TextView tvTitle;
        TextView tvDescription;
        ImageView imgLike;
        TextView tvLikeCount;

        public CustomViewHolder(View view) {
            super(view);
            imgContent = view.findViewById(R.id.img_content);
            tvTitle = view.findViewById(R.id.tv_title);
            tvDescription = view.findViewById(R.id.tv_description);
            imgLike = view.findViewById(R.id.img_like);
            tvLikeCount = view.findViewById(R.id.tv_like_count);
        }
    }
}